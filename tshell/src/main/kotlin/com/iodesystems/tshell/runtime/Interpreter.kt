package com.iodesystems.tshell.runtime

import com.iodesystems.tshell.parser.TShellLexer
import com.iodesystems.tshell.parser.TShellParser
import com.iodesystems.tshell.parser.TShellParser.*
import kotlinx.coroutines.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

private class FieldStep(val name: String)
private class IndexStep(val value: TShellValue)

private data class CallArgs(
  val positional: List<TShellValue>,
  val named: LinkedHashMap<String, TShellValue>
) {
  val hasNamed get() = named.isNotEmpty()

  companion object {
    val EMPTY = CallArgs(emptyList(), linkedMapOf())
  }
}

data class EvalResult(
  val value: TShellValue,
  val exportedNames: Set<String>
)

class Interpreter(
  private val commands: CommandRegistry,
  private val globals: Environment = Environment(),
  private val limits: ExecutionLimits = ExecutionLimits()
) {

  init {
    initBuiltinBindings()
  }

  fun eval(source: String): EvalResult = runBlocking(Dispatchers.Default) {
    evalAsync(source)
  }

  suspend fun evalAsync(source: String): EvalResult {
    val tree = parse(source)
    val visitor = Visitor(globals)
    val value = try {
      visitor.eval(tree)
    } catch (r: ReturnSignal) {
      r.value
    }
    return EvalResult(value, visitor.exportedNames)
  }

  private fun parse(source: String): ProgramContext {
    val lexer = TShellLexer(CharStreams.fromString(source))
    lexer.removeErrorListeners()
    lexer.addErrorListener(DescriptiveErrorListener(source))
    val tokens = CommonTokenStream(lexer)
    val parser = TShellParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(DescriptiveErrorListener(source))
    return parser.program()
  }

  /**
   * Executes a TFunction in a fresh Visitor.
   * Used by all() to run branches in parallel, and by TFunction.call()
   * from native commands (map, filter, reduce) to execute user functions.
   */
  suspend fun executeInBranch(fn: TShellValue.TFunction, args: List<TShellValue>): TShellValue {
    val visitor = Visitor(globals)
    return visitor.callFunctionInternal(fn, args)
  }

  // --- Built-in bindings (namespaces + composition fns) ---

  /**
   * Registers a namespace as a TObject in globals, with optional global-scope fallback for each method.
   * @param name The namespace identifier (e.g. "Promise", "JSON")
   * @param methods Map of method name → TFunction
   * @param globalFallback If true, each method is also defined at global scope
   */
  private fun registerNamespace(name: String, methods: Map<String, TShellValue.TFunction>, globalFallback: Boolean = false) {
    val obj = TShellValue.TObject(methods.mapValues { it.value as TShellValue })
    globals.defineOrSet(name, obj)
    if (globalFallback) {
      for ((methodName, fn) in methods) {
        if (globals.get(methodName) == null) {
          globals.defineOrSet(methodName, fn)
        }
      }
    }
  }

  private fun nativeFn(name: String, fn: suspend (List<TShellValue>) -> TShellValue): TShellValue.TFunction =
    TShellValue.TFunction(name = name, params = emptyList(), body = TShellValue.FunctionBody.Native(fn))

  private fun commandFn(cmdName: String): TShellValue.TFunction? {
    val cmd = commands.get(cmdName) ?: return null
    return nativeFn(cmdName, cmd.fn)
  }

  private fun initBuiltinBindings() {
    // Composition functions — close over this Interpreter for parallel execution
    val allFn = nativeFn("all") { args -> executeAll(args) }
    val raceFn = nativeFn("race") { args -> executeRace(args) }
    val anyFn = nativeFn("any") { args -> executeAny(args) }
    val chainFn = nativeFn("chain") { args -> executeChain(args) }

    // Register at global scope (always update — these close over this interpreter's limits)
    globals.defineOrSet("all", allFn)
    globals.defineOrSet("race", raceFn)
    globals.defineOrSet("any", anyFn)
    globals.defineOrSet("chain", chainFn)

    // JS constructor aliases — String(x) → str(x), Number("42") → num("42"), etc.
    for ((jsName, tshellCmd) in JS_CONSTRUCTOR_ALIASES) {
      if (globals.get(jsName) != null) continue
      commandFn(tshellCmd)?.let { globals.defineOrSet(jsName, it) }
    }

    // JS namespaces — only define if not already set (toolkits may install richer versions)
    for ((nsName, mapping) in JS_NAMESPACE_ALIASES) {
      if (globals.get(nsName) != null) continue
      val methods = mutableMapOf<String, TShellValue.TFunction>()
      for ((jsMethod, tshellCmd) in mapping) {
        commandFn(tshellCmd)?.let { methods[jsMethod] = it }
      }
      if (methods.isNotEmpty()) {
        registerNamespace(nsName, methods, globalFallback = false)
      }
    }

    // Array namespace — callable: Array(n) creates array of n nulls
    if (globals.get("Array") == null || globals.get("Array") is TShellValue.TObject) {
      val arrayMethods = mutableMapOf<String, TShellValue>()
      for ((jsMethod, tshellCmd) in JS_NAMESPACE_ALIASES["Array"].orEmpty()) {
        commandFn(tshellCmd)?.let { arrayMethods[jsMethod] = it }
      }
      arrayMethods["__call"] = nativeFn("Array") { args ->
        val n = (args.firstOrNull() as? TShellValue.TNumber)?.value?.toInt() ?: 0
        TShellValue.TArray(List(n) { TShellValue.TNull })
      }
      globals.defineOrSet("Array", TShellValue.TObject(arrayMethods))
    }

    // Promise namespace (always update — uses interpreter-level fns)
    registerNamespace("Promise", mapOf("all" to allFn, "race" to raceFn), globalFallback = false)
  }

  // --- Composition execution ---

  private suspend fun executeAll(args: List<TShellValue>): TShellValue {
    val fns = requireFnArgs("all", args)
    if (fns.size <= 1) {
      return TShellValue.TArray(fns.map { it.callAsync(emptyList()) })
    }
    val branchLimitsList = fns.map { limits.fork() }
    val results = runParallel(branchLimitsList) { idx ->
      val branchInterpreter = Interpreter(commands, globals, branchLimitsList[idx])
      branchInterpreter.executeInBranch(fns[idx], emptyList())
    }
    return TShellValue.TArray(results)
  }

  private suspend fun executeRace(args: List<TShellValue>): TShellValue {
    val fns = requireFnArgs("race", args)
    if (fns.isEmpty()) throw TShellError.runtime("race() — no producers given")
    if (fns.size == 1) return fns[0].callAsync(emptyList())

    val branchLimitsList = fns.map { limits.fork() }
    val remainingMs = limits.timeoutMs - (System.currentTimeMillis() - limits.startTimeMs.get())
    if (remainingMs <= 0) throw TShellError.runtime("race() — timeout already exceeded")
    val channel = kotlinx.coroutines.channels.Channel<Result<TShellValue>>(fns.size)
    return coroutineScope {
      val jobs = fns.mapIndexed { idx, fn ->
        launch {
          val branchInterpreter = Interpreter(commands, globals, branchLimitsList[idx])
          try {
            val value = branchInterpreter.executeInBranch(fn, emptyList())
            channel.send(Result.success(value))
          } catch (e: TShellError) {
            channel.send(Result.failure(e))
          } catch (e: Exception) {
            channel.send(Result.failure(TShellError.runtime("race branch failed: ${e.message}")))
          }
        }
      }
      var failures = 0
      var winner: TShellValue? = null
      val timeoutResult = withTimeoutOrNull(remainingMs) {
        repeat(fns.size) {
          val r = channel.receive()
          if (r.isSuccess && winner == null) {
            winner = r.getOrThrow()
            branchLimitsList.forEach { it.cancel() }
          } else if (r.isFailure) {
            failures++
          }
        }
      }
      if (timeoutResult == null) {
        branchLimitsList.forEach { it.cancel() }
        jobs.forEach { it.cancel() }
        throw TShellError.runtime("race() — timeout exceeded (${limits.timeoutMs}ms)")
      }
      jobs.forEach { it.cancel() }
      channel.close()
      winner ?: throw TShellError.runtime("race() — all ${fns.size} producers failed")
    }
  }

  private suspend fun executeAny(args: List<TShellValue>): TShellValue {
    val fns = requireFnArgs("any", args)
    if (fns.isEmpty()) throw TShellError.runtime("any() — no producers given")
    val errors = mutableListOf<String>()
    for ((idx, fn) in fns.withIndex()) {
      try {
        val result = fn.callAsync(emptyList())
        if (result.isTruthy()) return result
      } catch (e: TShellError) {
        errors.add("  [${idx + 1}] ${e.message}")
      }
    }
    throw TShellError.runtime(
      "any() — no producer returned a truthy value\n\n" +
        if (errors.isNotEmpty()) "Errors:\n${errors.joinToString("\n")}" else "All ${fns.size} producers returned falsy values"
    )
  }

  private suspend fun executeChain(args: List<TShellValue>): TShellValue {
    val fns = requireFnArgs("chain", args)
    if (fns.isEmpty()) throw TShellError.runtime("chain() requires at least one function argument")
    var result: TShellValue = TShellValue.TNull
    for ((idx, fn) in fns.withIndex()) {
      result = if (idx == 0) fn.callAsync(emptyList()) else fn.callAsync(listOf(result))
    }
    return result
  }

  private fun requireFnArgs(name: String, args: List<TShellValue>): List<TShellValue.TFunction> =
    args.map { arg ->
      when (arg) {
        is TShellValue.TFunction -> arg
        else -> throw TShellError.wrongArguments(name, "...fns: function[]", args,
          "$name(() => a(), () => b())")
      }
    }

  private suspend fun runParallel(branchLimitsList: List<ExecutionLimits>, work: suspend (Int) -> TShellValue): List<TShellValue> {
    val remainingMs = limits.timeoutMs - (System.currentTimeMillis() - limits.startTimeMs.get())
    if (remainingMs <= 0) throw TShellError.runtime("Timeout already exceeded before parallel execution")
    return coroutineScope {
      val result = withTimeoutOrNull(remainingMs) {
        branchLimitsList.mapIndexed { idx, _ ->
          async { work(idx) }
        }.awaitAll()
      }
      if (result == null) {
        branchLimitsList.forEach { it.cancel() }
        throw TShellError.runtime("Execution timeout exceeded (${limits.timeoutMs}ms) during parallel execution")
      }
      result
    }
  }

  private class DescriptiveErrorListener(private val source: String) : BaseErrorListener() {
    override fun syntaxError(
      recognizer: Recognizer<*, *>?,
      offendingSymbol: Any?,
      line: Int,
      col: Int,
      msg: String,
      e: RecognitionException?
    ) {
      val lines = source.lines()
      val sourceLine = if (line <= lines.size) lines[line - 1] else ""
      val pointer = " ".repeat(col) + "^"
      val friendly = translateError(msg, sourceLine, col, offendingSymbol)
      val escapeHint = if (sourceLine.contains("\\\\") || sourceLine.contains("\\\"") || sourceLine.contains("\\'"))
        "\n\n  Hint: strings with backslashes are error-prone in code. " +
        "Use the vars parameter to pass complex strings directly, " +
        "or use r\"...\" / r'...' / r`...` for raw strings where backslashes stay literal."
      else ""
      throw TShellError(
        "Syntax error at line $line:$col\n\n" +
          "  $sourceLine\n" +
          "  $pointer\n\n" +
          "  $friendly$escapeHint"
      )
    }

    private fun translateError(msg: String, sourceLine: String, col: Int, offendingSymbol: Any?): String {
      val got = (offendingSymbol as? Token)?.text ?: ""
      val before = sourceLine.substring(0, col.coerceAtMost(sourceLine.length)).trimEnd()

      // Translate ANTLR's cryptic "expecting {...}" into human-readable messages
      return when {
        // Missing expression (after =, :, return, etc.)
        msg.contains("expecting") && msg.contains("NUMBER") && msg.contains("IDENTIFIER") && msg.contains("STRING") ->
          when {
            before.endsWith("=") -> "Expected an expression after '='\n\n  Example: let x = 42"
            before.endsWith(":") -> "Expected an expression after ':'\n\n  Example: {key: value}"
            before.endsWith("return") -> "Expected an expression after 'return'\n\n  Example: return x + 1"
            before.endsWith("(") -> "Expected an expression or ')'\n\n  Example: fn(arg1, arg2)"
            before.endsWith(",") -> "Expected an expression after ','\n\n  Example: [1, 2, 3]"
            got == "<EOF>" -> "Unexpected end of input — expression is incomplete"
            else -> "Expected an expression, got '$got'"
          }

        // Missing let in for loop
        msg.contains("missing") && msg.contains("'let'") ->
          "for loops require 'let' before the variable\n\n  Example: for (let item of items) { ... }"

        // Missing identifier (after val, let, fn, etc.)
        msg.contains("expecting") && msg.contains("IDENTIFIER") && !msg.contains("NUMBER") ->
          when {
            before.endsWith("let") ->
              "Expected a variable name after 'let'\n\n  Example: let name = value"
            before.endsWith("function") ->
              "Expected a function name after 'function'\n\n  Example: function myFunction(arg) { ... }"
            before.endsWith(".") ->
              "Expected a property name after '.'\n\n  Example: obj.property"
            else -> "Expected an identifier, got '$got'"
          }

        // Missing closing delimiter
        msg.contains("expecting") && msg.contains("')'") && !msg.contains("IDENTIFIER") ->
          "Expected ')' to close the parentheses\n\n  Check for missing or extra arguments"

        msg.contains("expecting") && msg.contains("']'") ->
          "Expected ']' to close the array\n\n  Example: [1, 2, 3]"

        msg.contains("expecting") && msg.contains("'}'") ->
          "Expected '}' to close the block or object"

        // Mismatched input in specific contexts
        msg.contains("mismatched input") && msg.contains("'{'") && msg.contains("')'") ->
          "Expected ')' before '{'\n\n  Check that function parameters are properly closed\n  Example: fn name(param1, param2) { ... }"

        // Extraneous input
        msg.contains("extraneous input") ->
          "Unexpected '$got' here\n\n  Check for typos or missing operators"

        // No viable alternative
        msg.contains("no viable alternative") ->
          "Unexpected syntax at '$got'\n\n  This doesn't look like a valid statement or expression"

        // Default: pass through ANTLR's message but add a hint
        else -> "$msg\n\n  Hint: check for missing operators, unclosed brackets, or typos"
      }
    }
  }

  private class ReturnSignal(val value: TShellValue) : RuntimeException()
  private class BreakSignal : RuntimeException()
  private class ContinueSignal : RuntimeException()

  private inner class Visitor(private var env: Environment) {

    val exportedNames = mutableSetOf<String>()

    /** Cache: command name → wrapped TFunction. Avoids re-wrapping on every identifier access. */
    private val commandFnCache = HashMap<String, TShellValue.TFunction>()

    private fun step(ctx: ParserRuleContext) {
      limits.step(ctx.start?.line ?: 0)
    }

    private fun pushCall(ctx: ParserRuleContext) {
      limits.pushCall(ctx.start?.line ?: 0)
    }

    private fun popCall() {
      limits.popCall()
    }

    private suspend fun <T> withLocation(ctx: ParserRuleContext, block: suspend () -> T): T {
      try {
        return block()
      } catch (e: TShellError) {
        if (!e.message!!.contains("at line")) {
          val line = ctx.start?.line ?: 0
          val col = ctx.start?.charPositionInLine ?: 0
          throw TShellError("${e.message}\n\n  at line $line:$col")
        }
        throw e
      }
    }

    /**
     * Central dispatch: maps parse tree nodes to visit methods.
     * Replaces ANTLR's generated visitor dispatch with a suspend-capable version.
     */
    suspend fun eval(ctx: ParseTree): TShellValue = when (ctx) {
      is ProgramContext -> visitProgram(ctx)
      // Statements
      is ExportStatementContext -> visitExportStatement(ctx)
      is LetDeclContext -> visitLetDecl(ctx)
      is FnDeclContext -> visitFnDecl(ctx)
      is TryCatchStatementContext -> visitTryCatchStatement(ctx)
      is ThrowStatementContext -> visitThrowStatement(ctx)
      is ReturnStatementContext -> visitReturnStatement(ctx)
      is BreakStatementContext -> visitBreakStatement(ctx)
      is ContinueStatementContext -> visitContinueStatement(ctx)
      is AssignStatementContext -> visitAssignStatement(ctx)
      is IncrDecrStatementContext -> visitIncrDecrStatement(ctx)
      is ExpressionStatementContext -> visitExpressionStatement(ctx)
      is IfStatementContext -> visitIfStatement(ctx)
      is SwitchStatementContext -> visitSwitchStatement(ctx)
      is WhileStatementContext -> visitWhileStatement(ctx)
      is DoWhileStatementContext -> visitDoWhileStatement(ctx)
      is ForOfStatementContext -> visitForOfStatement(ctx)
      is ForInStatementContext -> visitForInStatement(ctx)
      is ForStatementContext -> visitForStatement(ctx)
      is BlockContext -> visitBlock(ctx)
      is BlockOrStatementContext -> visitBlockOrStatement(ctx)
      // Expressions
      is ExpressionContext -> visitExpression(ctx)
      is TernaryExprContext -> visitTernaryExpr(ctx)
      is NullCoalesceExprContext -> visitNullCoalesceExpr(ctx)
      is OrExprContext -> visitOrExpr(ctx)
      is AndExprContext -> visitAndExpr(ctx)
      is BitwiseOrExprContext -> visitBitwiseOrExpr(ctx)
      is BitwiseXorExprContext -> visitBitwiseXorExpr(ctx)
      is BitwiseAndExprContext -> visitBitwiseAndExpr(ctx)
      is EqualityExprContext -> visitEqualityExpr(ctx)
      is ComparisonExprContext -> visitComparisonExpr(ctx)
      is ShiftExprContext -> visitShiftExpr(ctx)
      is PipeExprContext -> visitPipeExpr(ctx)
      is AdditiveExprContext -> visitAdditiveExpr(ctx)
      is MultiplicativeExprContext -> visitMultiplicativeExpr(ctx)
      is UnaryExprContext -> visitUnaryExpr(ctx)
      is PostfixExprContext -> visitPostfixExpr(ctx)
      // Primary expressions
      is NumberLiteralContext -> visitNumberLiteral(ctx)
      is StringLiteralContext -> visitStringLiteral(ctx)
      is RawStringLiteralContext -> visitRawStringLiteral(ctx)
      is TemplateLiteralContext -> visitTemplateLiteral(ctx)
      is RawTemplateLiteralContext -> visitRawTemplateLiteral(ctx)
      is TrueLiteralContext -> visitTrueLiteral(ctx)
      is FalseLiteralContext -> visitFalseLiteral(ctx)
      is NullLiteralContext -> visitNullLiteral(ctx)
      is IdentifierExprContext -> visitIdentifierExpr(ctx)
      is RegexExprContext -> visitRegexExpr(ctx)
      is ArrayExprContext -> visitArrayExpr(ctx)
      is ObjectExprContext -> visitObjectExpr(ctx)
      is FuncExprContext -> visitFuncExpr(ctx)
      is ParenExprContext -> visitParenExpr(ctx)
      // Arrow functions (ArrowExprContext wraps arrowFunction rule)
      is ArrowExprContext -> eval(ctx.arrowFunction())
      is SingleParamArrowContext -> visitSingleParamArrow(ctx)
      is MultiParamArrowContext -> visitMultiParamArrow(ctx)
      is SingleParamArrowBlockContext -> visitSingleParamArrowBlock(ctx)
      is MultiParamArrowBlockContext -> visitMultiParamArrowBlock(ctx)
      // Compound literals
      is ArrayLiteralContext -> visitArrayLiteral(ctx)
      is ObjectLiteralContext -> visitObjectLiteral(ctx)
      is TemplateStringContext -> visitTemplateString(ctx)
      is RawTemplateStringContext -> visitRawTemplateString(ctx)
      // Statement wrapper (grammar's statement rule dispatches to sub-rules)
      is StatementContext -> {
        val child = ctx.getChild(0) ?: throw TShellError.runtime("Empty statement")
        if (child is org.antlr.v4.runtime.tree.TerminalNode) TShellValue.TNull // bare semicolons
        else eval(child)
      }
      else -> throw TShellError.runtime("Unknown node type: ${ctx::class.simpleName}")
    }

    suspend fun visitProgram(ctx: ProgramContext): TShellValue {
      var result: TShellValue = TShellValue.TNull
      for (stmt in ctx.statement()) {
        step(stmt)
        result = eval(stmt)
      }
      return result
    }

    // --- Statements ---

    suspend fun visitExportStatement(ctx: ExportStatementContext): TShellValue {
      // Execute the inner statement
      val result = when {
        ctx.letDecl() != null -> visitLetDecl(ctx.letDecl())
        ctx.fnDecl() != null -> visitFnDecl(ctx.fnDecl())
        ctx.assignStatement() != null -> visitAssignStatement(ctx.assignStatement())
        else -> TShellValue.TNull
      }
      // Record the names that were defined
      when {
        ctx.letDecl() != null -> ctx.letDecl().letBinding().forEach { collectDestructureNames(it.destructure(), exportedNames) }
        ctx.fnDecl() != null -> exportedNames.add(ctx.fnDecl().IDENTIFIER()?.text ?: ctx.fnDecl().FUNCTION(1)?.text ?: "")
        ctx.assignStatement() != null -> {
          val at = ctx.assignStatement().assignTarget()
          exportedNames.add(identOrFunctionText(at.IDENTIFIER(), at.FUNCTION()))
        }
      }
      return result
    }

    private fun collectDestructureNames(ctx: DestructureContext, names: MutableSet<String>) {
      when {
        ctx.IDENTIFIER() != null -> names.add(ctx.IDENTIFIER().text)
        ctx.FUNCTION() != null -> names.add(ctx.FUNCTION().text)
        ctx.objectDestructure() != null -> {
          for (field in ctx.objectDestructure().destructureField()) {
            val targetDestructure = field.destructure()
            if (targetDestructure != null) {
              collectDestructureNames(targetDestructure, names)
            } else {
              names.add(identOrFunctionText(field.IDENTIFIER(), field.FUNCTION()))
            }
          }
        }
        ctx.arrayDestructure() != null -> {
          for (dest in ctx.arrayDestructure().destructure()) {
            collectDestructureNames(dest, names)
          }
        }
      }
    }

    suspend fun visitLetDecl(ctx: LetDeclContext): TShellValue {
      var lastValue: TShellValue = TShellValue.TNull
      for (binding in ctx.letBinding()) {
        lastValue = if (binding.expression() != null) eval(binding.expression()) else TShellValue.TNull
        bindDestructure(binding.destructure(), lastValue)
      }
      return lastValue
    }

    suspend fun visitFnDecl(ctx: FnDeclContext): TShellValue {
      val name = ctx.IDENTIFIER()?.text ?: ctx.FUNCTION(1)?.text ?: throw TShellError.runtime("Expected function name")
      val params = ctx.paramList()?.param()?.map { paramName(it) } ?: emptyList()
      val capturedEnv = env
      val fn = TShellValue.TFunction(
        name = name,
        params = params,
        body = TShellValue.FunctionBody.Block(ctx.block(), capturedEnv, commands, limits)
      )
      env.defineOrSet(name, fn)
      return TShellValue.TNull
    }

    suspend fun visitFuncExpr(ctx: FuncExprContext): TShellValue {
      val fCtx = ctx.functionExpr()
      val name = fCtx.IDENTIFIER()?.text ?: fCtx.FUNCTION(1)?.text
      val params = fCtx.paramList()?.param()?.map { paramName(it) } ?: emptyList()
      // Named function expressions get a child env with self-reference for recursion
      val closureEnv = if (name != null) env.child() else env
      val fn = TShellValue.TFunction(
        name = name ?: "<anonymous>",
        params = params,
        body = TShellValue.FunctionBody.Block(fCtx.block(), closureEnv, commands, limits)
      )
      if (name != null) closureEnv.define(name, fn)
      return fn
    }

    suspend fun visitReturnStatement(ctx: ReturnStatementContext): TShellValue {
      val value = if (ctx.expression() != null) eval(ctx.expression()) else TShellValue.TNull
      throw ReturnSignal(value)
    }

    suspend fun visitTryCatchStatement(ctx: TryCatchStatementContext): TShellValue {
      var result: TShellValue = TShellValue.TNull
      try {
        result = visitBlock(ctx.block(0))
      } catch (e: TShellError) {
        val catchBlock = if (ctx.CATCH() != null) ctx.block(1) else null
        if (catchBlock != null) {
          val catchEnv = env.child()
          catchEnv.define(ctx.IDENTIFIER()?.text ?: ctx.FUNCTION()?.text ?: "e", TShellValue.TString(e.message ?: "unknown error"))
          val outerEnv = env
          env = catchEnv
          try {
            result = visitBlock(catchBlock)
          } finally {
            env = outerEnv
          }
        }
      } finally {
        val finallyBlock = if (ctx.FINALLY() != null) ctx.block().last() else null
        if (finallyBlock != null) {
          visitBlock(finallyBlock)
        }
      }
      return result
    }

    suspend fun visitThrowStatement(ctx: ThrowStatementContext): TShellValue {
      val value = eval(ctx.expression())
      val message = when (value) {
        is TShellValue.TString -> value.value
        else -> value.toDisplayString()
      }
      throw TShellError.runtime(message)
    }

    @Suppress("unused")
    suspend fun visitBreakStatement(ctx: BreakStatementContext): TShellValue {
      throw BreakSignal()
    }

    @Suppress("unused")
    suspend fun visitContinueStatement(ctx: ContinueStatementContext): TShellValue {
      throw ContinueSignal()
    }

    private suspend fun performAssign(target: AssignTargetContext, op: String, rhs: TShellValue) {
      val rootName = identOrFunctionText(target.IDENTIFIER(), target.FUNCTION())
      val steps = buildAccessorSteps(target)

      if (steps.isEmpty()) {
        val finalValue = if (op == "=") rhs else {
          val current = env.get(rootName) ?: throw TShellError.runtime("'$rootName' is not defined")
          applyCompoundOp(op, current, rhs)
        }
        env.set(rootName, finalValue)
      } else {
        // JS reference semantics: walk to the parent container and mutate in-place
        var current = env.get(rootName)
          ?: throw TShellError.runtime("'$rootName' is not defined")
        for (i in 0 until steps.size - 1) {
          current = resolveStep(current, steps[i])
        }
        val lastStep = steps.last()
        val finalValue = if (op == "=") rhs else {
          val oldValue = resolveStep(current, lastStep)
          applyCompoundOp(op, oldValue, rhs)
        }
        mutateInPlace(current, lastStep, finalValue)
      }
    }

    /** Mutate a container in-place at the given step (JS reference semantics). */
    private fun mutateInPlace(parent: TShellValue, step: Any, value: TShellValue) {
      when (step) {
        is FieldStep -> {
          val obj = parent as? TShellValue.TObject
            ?: throw TShellError.typeMismatch("assignment to .${step.name}", "object", parent)
          (obj.fields as MutableMap)[step.name] = value
        }
        is IndexStep -> when (parent) {
          is TShellValue.TObject -> {
            val key = when (step.value) {
              is TShellValue.TString -> step.value.value
              is TShellValue.TNumber -> step.value.toDisplayString()
              else -> throw TShellError.typeMismatch("index assignment", "string key", step.value)
            }
            (parent.fields as MutableMap)[key] = value
          }
          is TShellValue.TArray -> {
            val idx = (step.value as? TShellValue.TNumber)?.value?.toInt()
              ?: throw TShellError.typeMismatch("index assignment", "number", step.value)
            if (idx < 0) throw TShellError.runtime("Index $idx out of bounds (size ${parent.elements.size})")
            val elems = parent.elements as MutableList
            // Auto-grow with nulls (JS behavior)
            while (elems.size <= idx) elems.add(TShellValue.TNull)
            elems[idx] = value
          }
          else -> throw TShellError.typeMismatch("assignment", "object or array", parent)
        }
        else -> throw TShellError.runtime("Unknown accessor step")
      }
    }

    suspend fun visitAssignStatement(ctx: AssignStatementContext): TShellValue {
      val rhs = eval(ctx.expression())
      performAssign(ctx.assignTarget(), ctx.assignOp().text, rhs)
      return TShellValue.TNull
    }

    private suspend fun performIncrDecr(target: AssignTargetContext, op: String) {
      val delta = if (op == "++") TShellValue.TNumber(1.0) else TShellValue.TNumber(-1.0)
      performAssign(target, "+=", delta)
    }

    @Suppress("unused")
    suspend fun visitIncrDecrStatement(ctx: IncrDecrStatementContext): TShellValue {
      val op = if (ctx.INCREMENT() != null) "++" else "--"
      performIncrDecr(ctx.assignTarget(), op)
      return TShellValue.TNull
    }

    suspend fun visitExpressionStatement(ctx: ExpressionStatementContext): TShellValue {
      return eval(ctx.expression())
    }

    suspend fun visitExpression(ctx: ExpressionContext): TShellValue {
      return eval(ctx.ternaryExpr())
    }

    suspend fun visitBlockOrStatement(ctx: BlockOrStatementContext): TShellValue {
      return if (ctx.block() != null) {
        visitBlock(ctx.block())
      } else {
        eval(ctx.statement())
      }
    }

    suspend fun visitIfStatement(ctx: IfStatementContext): TShellValue {
      val condition = eval(ctx.expression())
      return if (condition.isTruthy()) {
        visitBlockOrStatement(ctx.blockOrStatement(0))
      } else if (ctx.ifStatement() != null) {
        visitIfStatement(ctx.ifStatement())
      } else if (ctx.blockOrStatement().size > 1) {
        visitBlockOrStatement(ctx.blockOrStatement(1))
      } else {
        TShellValue.TNull
      }
    }

    suspend fun visitSwitchStatement(ctx: SwitchStatementContext): TShellValue {
      val subject = eval(ctx.expression())
      var result: TShellValue = TShellValue.TNull
      var falling = false // true once a case matches (enables fall-through)
      try {
        for (case in ctx.switchCase()) {
          if (!falling) {
            val caseVal = eval(case.expression())
            if (!valueEquals(subject, caseVal)) continue
            falling = true
          }
          for (stmt in case.statement()) {
            result = eval(stmt)
          }
        }
        // Default: runs if no case matched, or if we fell through without break
        val default = ctx.switchDefault()
        if (default != null) {
          for (stmt in default.statement()) {
            result = eval(stmt)
          }
        }
      } catch (_: BreakSignal) {
        // break exits the switch
      }
      return result
    }

    suspend fun visitDoWhileStatement(ctx: DoWhileStatementContext): TShellValue {
      var result: TShellValue = TShellValue.TNull
      do {
        step(ctx)
        try {
          result = visitBlock(ctx.block())
        } catch (_: BreakSignal) {
          break
        } catch (_: ContinueSignal) {
          // fall through to condition check
        }
      } while (eval(ctx.expression()).isTruthy())
      return result
    }

    suspend fun visitWhileStatement(ctx: WhileStatementContext): TShellValue {
      var result: TShellValue = TShellValue.TNull
      while (eval(ctx.expression()).isTruthy()) {
        step(ctx)
        try {
          result = visitBlock(ctx.block())
        } catch (_: BreakSignal) {
          break
        } catch (_: ContinueSignal) {
          continue
        }
      }
      return result
    }

    suspend fun visitForOfStatement(ctx: ForOfStatementContext): TShellValue {
      val iterable = eval(ctx.expression())
      val items = when (iterable) {
        is TShellValue.TArray -> iterable.elements
        is TShellValue.TString -> iterable.value.map { TShellValue.TString(it.toString()) }
        else -> throw TShellError.typeMismatch("for..of", "array or string", iterable)
      }
      var result: TShellValue = TShellValue.TNull
      for (item in items) {
        step(ctx)
        val loopEnv = env.child()
        val outerEnv = env
        env = loopEnv
        bindDestructure(ctx.destructure(), item)
        try {
          result = visitBlock(ctx.block())
        } catch (_: BreakSignal) {
          env = outerEnv
          break
        } catch (_: ContinueSignal) {
          env = outerEnv
          continue
        }
        env = outerEnv
      }
      return result
    }

    suspend fun visitForInStatement(ctx: ForInStatementContext): TShellValue {
      val obj = eval(ctx.expression())
      val keys = when (obj) {
        is TShellValue.TObject -> obj.fields.keys.map { TShellValue.TString(it) }
        is TShellValue.TArray -> (0 until obj.elements.size).map { TShellValue.TNumber(it.toDouble()) }
        else -> throw TShellError.typeMismatch("for..in", "object or array", obj)
      }
      val varName = ctx.IDENTIFIER()?.text ?: ctx.FUNCTION()?.text ?: throw TShellError.runtime("Expected variable name in for..in")
      var result: TShellValue = TShellValue.TNull
      for (key in keys) {
        step(ctx)
        val loopEnv = env.child()
        val outerEnv = env
        env = loopEnv
        env.define(varName, key)
        try {
          result = visitBlock(ctx.block())
        } catch (_: BreakSignal) {
          env = outerEnv
          break
        } catch (_: ContinueSignal) {
          env = outerEnv
          continue
        }
        env = outerEnv
      }
      return result
    }

    suspend fun visitForStatement(ctx: ForStatementContext): TShellValue {
      val outerEnv = env
      env = env.child()
      // Init
      val letInit = ctx.forInitLet()
      val assignInit = ctx.forInitAssign()
      if (letInit != null) {
        val value = eval(letInit.expression())
        env.define(letInit.IDENTIFIER()?.text ?: letInit.FUNCTION()?.text ?: throw TShellError.runtime("Expected variable name in for init"), value)
      } else if (assignInit != null) {
        performAssign(assignInit.assignTarget(), assignInit.assignOp().text, eval(assignInit.expression()))
      }
      var result: TShellValue = TShellValue.TNull
      while (true) {
        step(ctx)
        // Condition
        val condCtx = ctx.expression()
        if (condCtx != null) {
          val cond = eval(condCtx)
          if (!cond.isTruthy()) break
        }
        try {
          result = visitBlock(ctx.block())
        } catch (_: BreakSignal) {
          break
        } catch (_: ContinueSignal) {
          // fall through to update
        }
        // Update
        val updateCtx = ctx.forUpdateAssign()
        val updateIncrDecr = ctx.forUpdateIncrDecr()
        if (updateCtx != null) {
          performAssign(updateCtx.assignTarget(), updateCtx.assignOp().text, eval(updateCtx.expression()))
        } else if (updateIncrDecr != null) {
          val op = if (updateIncrDecr.INCREMENT() != null) "++" else "--"
          performIncrDecr(updateIncrDecr.assignTarget(), op)
        }
      }
      env = outerEnv
      return result
    }

    suspend fun visitBlock(ctx: BlockContext): TShellValue {
      val blockEnv = env.child()
      val outerEnv = env
      env = blockEnv
      var result: TShellValue = TShellValue.TNull
      for (stmt in ctx.statement()) {
        result = eval(stmt)
      }
      env = outerEnv
      return result
    }

    // --- Expressions ---

    /** Normalize a value to an array: null→[], non-array→[value], array→array */
    private fun normalizeToArray(value: TShellValue): List<TShellValue> = when (value) {
      is TShellValue.TNull -> emptyList()
      is TShellValue.TArray -> value.elements
      else -> listOf(value)
    }

    suspend fun visitPipeExpr(ctx: PipeExprContext): TShellValue {
      val exprs = ctx.additiveExpr()
      if (exprs.size == 1) {
        return eval(exprs[0])
      }
      // Grammar: additiveExpr ((PIPE_RIGHT | PIPE_SCATTER) additiveExpr (PIPE_LEFT additiveExpr)*)*
      // Walk children to determine pipe structure
      var result = eval(exprs[0])
      var i = 1
      for (child in ctx.children.drop(1)) {
        if (child is org.antlr.v4.runtime.tree.TerminalNode &&
            child.symbol.type in listOf(TShellLexer.PIPE_RIGHT, TShellLexer.PIPE_SCATTER)) {
          step(ctx)
          val pipeType = child.symbol.type
          val rhsExpr = exprs[i++]

          // Destructure only for |>
          if (pipeType == TShellLexer.PIPE_RIGHT) {
            val destructNames = tryGetArrayDestructureNames(rhsExpr)
            if (destructNames != null) {
              val arr = result as? TShellValue.TArray
                ?: throw TShellError.typeMismatch("pipe destructure", "array", result,
                  "Left side of |> [names] must be an array")
              for ((idx, name) in destructNames.withIndex()) {
                val value = arr.elements.getOrElse(idx) { TShellValue.TNull }
                env.defineOrSet(name, value)
              }
              continue
            }
          }

          // Collect any <| args
          val rightArgs = mutableListOf<TShellValue>()
          var j = ctx.children.indexOf(child) + 2
          while (j < ctx.children.size) {
            val next = ctx.children[j]
            if (next is org.antlr.v4.runtime.tree.TerminalNode &&
                next.symbol.type == TShellLexer.PIPE_LEFT) {
              rightArgs.add(eval(exprs[i++]))
              j += 2
            } else {
              break
            }
          }

          // Resolve the RHS function (and any inline call args)
          val pipeCall = extractPipeCall(rhsExpr)
          val fn: TShellValue.TFunction
          val extraArgs: List<TShellValue>
          if (pipeCall != null) {
            fn = pipeCall.first
            extraArgs = pipeCall.second + rightArgs
          } else {
            val rhsVal = eval(rhsExpr)
            fn = rhsVal as? TShellValue.TFunction
              ?: throw TShellError.typeMismatch(
                if (pipeType == TShellLexer.PIPE_SCATTER) "scatter pipe" else "pipe",
                "function",
                rhsVal,
                "Right side of pipe must be a function"
              )
            extraArgs = rightArgs
          }

          result = when (pipeType) {
            TShellLexer.PIPE_RIGHT -> {
              // Normal pipe: inject result as first arg
              withLocation(ctx) {
                callFunction(fn, listOf(result) + extraArgs, ctx)
              }
            }
            TShellLexer.PIPE_SCATTER -> {
              // Scatter: normalize to array, parallel map fn over each element
              val elements = normalizeToArray(result)
              if (elements.isEmpty()) {
                TShellValue.TArray(emptyList())
              } else if (elements.size == 1) {
                TShellValue.TArray(listOf(
                  withLocation(ctx) { callFunction(fn, listOf(elements[0]) + extraArgs, ctx) }
                ))
              } else {
                val branchLimitsList = elements.map { limits.fork() }
                val results = runParallel(branchLimitsList) { idx ->
                  val branchInterpreter = Interpreter(commands, env, branchLimitsList[idx])
                  branchInterpreter.executeInBranch(fn, listOf(elements[idx]) + extraArgs)
                }
                TShellValue.TArray(results)
              }
            }
            else -> throw IllegalStateException("Unknown pipe type")
          }
        }
      }
      return result
    }

    /**
     * Extracts a function + call args from the pipe RHS if it's a simple function call.
     * Drills through additiveExpr → multiplicativeExpr → unaryExpr → postfixExpr.
     * If the postfixExpr ends with a call (LPAREN ... RPAREN), we evaluate the base
     * (everything except the final call) to get the function, and evaluate the call args
     * separately, so the pipe can inject the piped value as the first argument.
     *
     * Returns null if the RHS is not a simple function call (e.g., it's an arrow, a literal, etc.).
     */
    private suspend fun extractPipeCall(ctx: AdditiveExprContext): Pair<TShellValue.TFunction, List<TShellValue>>? {
      // Must be a single term (no + or - operators)
      if (ctx.multiplicativeExpr().size != 1) return null
      val mult = ctx.multiplicativeExpr(0)
      // Must be a single factor (no * or / operators)
      if (mult.unaryExpr().size != 1) return null
      val unary = mult.unaryExpr(0)
      // Must not have unary operators (! or -)
      val postfix = unary.postfixExpr() ?: return null
      val ops = postfix.postfixOp()
      if (ops.isEmpty()) return null

      // Last postfixOp must be a call (LPAREN)
      val lastOp = ops.last()
      if (lastOp.LPAREN() == null) return null
      // Skip optional-chain calls for now (foo?.())
      if (lastOp.OPTIONAL_CHAIN() != null) return null

      // Evaluate the base: primaryExpr + all postfixOps except the last call
      var base = eval(postfix.primaryExpr())
      for (op in ops.dropLast(1)) {
        val isOptional = op.OPTIONAL_CHAIN() != null
        if (isOptional && base is TShellValue.TNull) continue
        base = withLocation(op) {
          when {
            op.fieldName() != null -> accessMember(base, fieldNameText(op.fieldName()))
            op.LBRACKET() != null -> accessIndex(base, eval(op.expression()))
            op.LPAREN() != null -> {
              val callArgs = op.argumentList()?.let { evalCallArgs(it) } ?: CallArgs.EMPTY
              val fn = asCallable(base)
                ?: throw TShellError.typeMismatch("call", "function", base)
              val args = resolveNamedArgs(fn, callArgs, op)
              callFunction(fn, args, op)
            }
            else -> base
          }
        }
      }

      if (base !is TShellValue.TFunction) return null

      // Evaluate the call args from the final LPAREN...RPAREN
      val callArgs = lastOp.argumentList()?.let { evalCallArgs(it) } ?: CallArgs.EMPTY
      val args = resolveNamedArgs(base, callArgs, lastOp)
      return Pair(base, args)
    }

    /**
     * Checks if an additiveExpr is a bare array literal of identifiers like [a, b, c].
     * Returns the list of identifier names, or null if not a destructure pattern.
     */
    private fun tryGetArrayDestructureNames(ctx: AdditiveExprContext): List<String>? {
      if (ctx.multiplicativeExpr().size != 1) return null
      val mult = ctx.multiplicativeExpr(0)
      if (mult.unaryExpr().size != 1) return null
      val unary = mult.unaryExpr(0)
      val postfix = unary.postfixExpr() ?: return null
      if (postfix.postfixOp().isNotEmpty()) return null
      val primary = postfix.primaryExpr()
      if (primary !is ArrayExprContext) return null
      val arrayLit = primary.arrayLiteral()
      val names = mutableListOf<String>()
      for (elem in arrayLit.spreadOrExpr()) {
        if (elem.SPREAD() != null) return null
        val text = elem.expression().text.trim()
        if (!text.matches(Regex("[a-zA-Z_$][a-zA-Z0-9_$]*"))) return null
        names.add(text)
      }
      return if (names.isNotEmpty()) names else null
    }

    suspend fun visitTernaryExpr(ctx: TernaryExprContext): TShellValue {
      val condition = eval(ctx.nullCoalesceExpr())
      return if (ctx.expression().size == 2) {
        if (condition.isTruthy()) eval(ctx.expression(0))
        else eval(ctx.expression(1))
      } else {
        condition
      }
    }

    suspend fun visitNullCoalesceExpr(ctx: NullCoalesceExprContext): TShellValue {
      var result = eval(ctx.orExpr(0))
      for (i in 1 until ctx.orExpr().size) {
        if (result !is TShellValue.TNull) return result
        result = eval(ctx.orExpr(i))
      }
      return result
    }

    suspend fun visitOrExpr(ctx: OrExprContext): TShellValue {
      var result = eval(ctx.andExpr(0))
      for (i in 1 until ctx.andExpr().size) {
        if (result.isTruthy()) return result
        result = eval(ctx.andExpr(i))
      }
      return result
    }

    suspend fun visitAndExpr(ctx: AndExprContext): TShellValue {
      var result = eval(ctx.bitwiseOrExpr(0))
      for (i in 1 until ctx.bitwiseOrExpr().size) {
        if (!result.isTruthy()) return result
        result = eval(ctx.bitwiseOrExpr(i))
      }
      return result
    }

    suspend fun visitBitwiseOrExpr(ctx: BitwiseOrExprContext): TShellValue {
      var result = eval(ctx.bitwiseXorExpr(0))
      for (i in 1 until ctx.bitwiseXorExpr().size) {
        val right = eval(ctx.bitwiseXorExpr(i))
        result = intBitwiseOp(result, right, "|") { a, b -> a or b }
      }
      return result
    }

    suspend fun visitBitwiseXorExpr(ctx: BitwiseXorExprContext): TShellValue {
      var result = eval(ctx.bitwiseAndExpr(0))
      for (i in 1 until ctx.bitwiseAndExpr().size) {
        val right = eval(ctx.bitwiseAndExpr(i))
        result = intBitwiseOp(result, right, "^") { a, b -> a xor b }
      }
      return result
    }

    suspend fun visitBitwiseAndExpr(ctx: BitwiseAndExprContext): TShellValue {
      var result = eval(ctx.equalityExpr(0))
      for (i in 1 until ctx.equalityExpr().size) {
        val right = eval(ctx.equalityExpr(i))
        result = intBitwiseOp(result, right, "&") { a, b -> a and b }
      }
      return result
    }

    suspend fun visitEqualityExpr(ctx: EqualityExprContext): TShellValue {
      var result = eval(ctx.comparisonExpr(0))
      for (i in 1 until ctx.comparisonExpr().size) {
        val right = eval(ctx.comparisonExpr(i))
        val op = ctx.getChild(2 * i - 1).text
        result = TShellValue.TBoolean(
          when (op) {
            "==", "===" -> valueEquals(result, right)
            "!=", "!==" -> !valueEquals(result, right)
            else -> false
          }
        )
      }
      return result
    }

    suspend fun visitComparisonExpr(ctx: ComparisonExprContext): TShellValue {
      var result = eval(ctx.shiftExpr(0))
      for (i in 1 until ctx.shiftExpr().size) {
        val right = eval(ctx.shiftExpr(i))
        val op = ctx.getChild(2 * i - 1).text
        result = if (op == "in") {
          TShellValue.TBoolean(
            when (right) {
              is TShellValue.TObject -> (result as? TShellValue.TString)?.value?.let { right.fields.containsKey(it) } ?: false
              is TShellValue.TArray -> right.elements.any { valueEquals(result, it) }
              else -> throw TShellError.typeMismatch("'in'", "object or array", right)
            }
          )
        } else {
          val cmp = compareValues(result, right)
          TShellValue.TBoolean(
            when (op) {
              "<" -> cmp < 0
              ">" -> cmp > 0
              "<=" -> cmp <= 0
              ">=" -> cmp >= 0
              else -> false
            }
          )
        }
      }
      return result
    }

    suspend fun visitShiftExpr(ctx: ShiftExprContext): TShellValue {
      var result = eval(ctx.pipeExpr(0))
      for (i in 1 until ctx.pipeExpr().size) {
        val right = eval(ctx.pipeExpr(i))
        val op = ctx.getChild(2 * i - 1).text
        result = when (op) {
          "<<" -> intBitwiseOp(result, right, "<<") { a, b -> a shl (b and 0x1f) }
          ">>" -> intBitwiseOp(result, right, ">>") { a, b -> a shr (b and 0x1f) }
          ">>>" -> intBitwiseOp(result, right, ">>>") { a, b -> a ushr (b and 0x1f) }
          else -> result
        }
      }
      return result
    }

    suspend fun visitAdditiveExpr(ctx: AdditiveExprContext): TShellValue {
      var result = eval(ctx.multiplicativeExpr(0))
      for (i in 1 until ctx.multiplicativeExpr().size) {
        val right = eval(ctx.multiplicativeExpr(i))
        val op = ctx.getChild(2 * i - 1).text
        result = when (op) {
          "+" -> add(result, right)
          "-" -> numericOp(result, right, "-") { a, b -> a - b }
          else -> result
        }
      }
      return result
    }

    suspend fun visitMultiplicativeExpr(ctx: MultiplicativeExprContext): TShellValue {
      var result = eval(ctx.unaryExpr(0))
      for (i in 1 until ctx.unaryExpr().size) {
        val right = eval(ctx.unaryExpr(i))
        val op = ctx.getChild(2 * i - 1).text
        result = when (op) {
          "*" -> numericOp(result, right, "*") { a, b -> a * b }
          "/" -> numericOp(result, right, "/") { a, b -> a / b }
          "%" -> numericOp(result, right, "%") { a, b -> a % b }
          else -> result
        }
      }
      return result
    }

    suspend fun visitUnaryExpr(ctx: UnaryExprContext): TShellValue {
      if (ctx.NOT() != null) {
        val v = eval(ctx.unaryExpr())
        return TShellValue.TBoolean(!v.isTruthy())
      }
      if (ctx.MINUS() != null) {
        val v = eval(ctx.unaryExpr())
        return when (v) {
          is TShellValue.TNumber -> TShellValue.TNumber(-v.value)
          else -> throw TShellError.typeMismatch("unary -", "number", v)
        }
      }
      if (ctx.TILDE() != null) {
        val v = eval(ctx.unaryExpr())
        return when (v) {
          is TShellValue.TNumber -> TShellValue.TNumber(v.value.toInt().inv().toDouble())
          else -> throw TShellError.typeMismatch("unary ~", "number", v)
        }
      }
      if (ctx.TYPEOF() != null) {
        val v = eval(ctx.unaryExpr())
        return TShellValue.TString(v.typeName())
      }
      return eval(ctx.postfixExpr())
    }

    suspend fun visitPostfixExpr(ctx: PostfixExprContext): TShellValue {
      var result = eval(ctx.primaryExpr())
      // Track lvalue path for mutating method writeback (e.g. arr.push(x) → reassign arr)
      val lvaluePath = mutableListOf<String>()
      val primaryIdent = (ctx.primaryExpr() as? IdentifierExprContext)?.let { it.IDENTIFIER()?.text ?: it.FUNCTION()?.text }
      if (primaryIdent != null) lvaluePath.add(primaryIdent)

      for ((opIdx, op) in ctx.postfixOp().withIndex()) {
        val isOptional = op.OPTIONAL_CHAIN() != null
        if (isOptional && result is TShellValue.TNull) {
          // Short-circuit: null?.anything = null
          continue
        }

        val current = result
        result = withLocation(op) {
          when {
            op.fieldName() != null -> {
              val field = fieldNameText(op.fieldName())
              // Check if next op is a call and this is a mutating array method
              val nextOp = ctx.postfixOp().getOrNull(opIdx + 1)
              if (nextOp?.LPAREN() != null && current is TShellValue.TArray && field in MUTATING_ARRAY_METHODS && lvaluePath.isNotEmpty()) {
                // Return a function that performs the mutation and writes back
                bindMutatingArrayMethod(current, field, lvaluePath.toList())
              } else {
                lvaluePath.add(field)
                accessMember(current, field)
              }
            }
            op.LBRACKET() != null -> {
              val index = eval(op.expression())
              // Track string-keyed object access for mutating method writeback (e.g. acc[key].push(x))
              if (current is TShellValue.TObject && index is TShellValue.TString) {
                lvaluePath.add(index.value)
              } else {
                lvaluePath.clear() // can't track non-string index paths for writeback
              }
              accessIndex(current, index)
            }
            op.LPAREN() != null -> {
              val callArgs = op.argumentList()?.let { evalCallArgs(it) } ?: CallArgs.EMPTY
              val fn = asCallable(current)
                ?: throw TShellError.typeMismatch("call", "function", current)
              val args = resolveNamedArgs(fn, callArgs, op)
              callFunction(fn, args, op)
            }
            op.INCREMENT() != null || op.DECREMENT() != null -> {
              // Postfix increment/decrement: return old value, mutate the variable
              val oldValue = current
              val delta = if (op.INCREMENT() != null) TShellValue.TNumber(1.0) else TShellValue.TNumber(-1.0)
              val newValue = applyCompoundOp("+=", current, delta)
              if (lvaluePath.size == 1) {
                env.set(lvaluePath[0], newValue)
              } else {
                throw TShellError.runtime("Postfix ${if (op.INCREMENT() != null) "++" else "--"} requires a simple variable")
              }
              oldValue
            }
            else -> current
          }
        }
      }
      return result
    }

    private fun bindMutatingArrayMethod(
      arr: TShellValue.TArray,
      method: String,
      lvaluePath: List<String>
    ): TShellValue.TFunction {
      return TShellValue.TFunction(
        name = method,
        params = emptyList(),
        body = TShellValue.FunctionBody.Native { args ->
          // Mutate the array in-place (JS reference semantics)
          env.mutate(lvaluePath) { current ->
            val currentArr = current as? TShellValue.TArray ?: arr
            val elems = currentArr.elements as MutableList
            when (method) {
              "push" -> { elems.addAll(args); currentArr }
              "pop" -> { if (elems.isNotEmpty()) elems.removeAt(elems.size - 1); currentArr }
              "shift" -> { if (elems.isNotEmpty()) elems.removeAt(0); currentArr }
              "unshift" -> { elems.addAll(0, args); currentArr }
              "splice" -> {
                val start = ((args.getOrNull(0) as? TShellValue.TNumber)?.value?.toInt() ?: 0)
                  .coerceIn(0, elems.size)
                val deleteCount = ((args.getOrNull(1) as? TShellValue.TNumber)?.value?.toInt() ?: elems.size)
                  .coerceIn(0, elems.size - start)
                val inserts = args.drop(2)
                elems.subList(start, start + deleteCount).clear()
                elems.addAll(start, inserts)
                currentArr
              }
              else -> currentArr
            }
          }
        }
      )
    }

    // --- Primary expressions ---

    @Suppress("unused")
    suspend fun visitRegexExpr(ctx: RegexExprContext): TShellValue {
      val text = ctx.REGEX().text
      // Format: /pattern/flags — strip leading /, split on last /
      val lastSlash = text.lastIndexOf('/')
      val pattern = text.substring(1, lastSlash)
      val flags = text.substring(lastSlash + 1)
      // Validate the regex
      try {
        Regex(pattern)
      } catch (e: Exception) {
        throw TShellError.runtime("Invalid regex /$pattern/$flags: ${e.message}")
      }
      return TShellValue.TRegex(pattern, flags)
    }

    @Suppress("unused")
    suspend fun visitNumberLiteral(ctx: NumberLiteralContext): TShellValue =
      TShellValue.TNumber(ctx.NUMBER().text.toDouble())

    @Suppress("unused")
    suspend fun visitStringLiteral(ctx: StringLiteralContext): TShellValue {
      val raw = ctx.STRING().text
      val inner = raw.substring(1, raw.length - 1)
      return TShellValue.TString(unescapeString(inner))
    }

    @Suppress("unused")
    suspend fun visitRawStringLiteral(ctx: RawStringLiteralContext): TShellValue {
      val raw = ctx.RAW_STRING().text
      // Strip r" and trailing " (or r' and ')
      val inner = raw.substring(2, raw.length - 1)
      return TShellValue.TString(inner)
    }

    @Suppress("unused")
    suspend fun visitTemplateLiteral(ctx: TemplateLiteralContext): TShellValue =
      eval(ctx.templateString())

    suspend fun visitTemplateString(ctx: TemplateStringContext): TShellValue {
      val sb = StringBuilder()
      for (part in ctx.templatePart()) {
        sb.append(
          when {
            part is TemplateTextContext -> unescapeString(part.TEMPLATE_TEXT().text)
            part is TemplateInterpContext -> {
              val v = eval(part.expression())
              v.toDisplayString()
            }
            else -> ""
          }
        )
      }
      return TShellValue.TString(sb.toString())
    }

    @Suppress("unused")
    suspend fun visitRawTemplateLiteral(ctx: RawTemplateLiteralContext): TShellValue =
      eval(ctx.rawTemplateString())

    suspend fun visitRawTemplateString(ctx: RawTemplateStringContext): TShellValue {
      val sb = StringBuilder()
      for (part in ctx.templatePart()) {
        sb.append(
          when {
            part is TemplateTextContext -> part.TEMPLATE_TEXT().text
            part is TemplateInterpContext -> {
              val v = eval(part.expression())
              v.toDisplayString()
            }
            else -> ""
          }
        )
      }
      return TShellValue.TString(sb.toString())
    }

    @Suppress("unused")
    suspend fun visitTrueLiteral(ctx: TrueLiteralContext): TShellValue = TShellValue.TBoolean(true)
    @Suppress("unused")
    suspend fun visitFalseLiteral(ctx: FalseLiteralContext): TShellValue = TShellValue.TBoolean(false)
    @Suppress("unused")
    suspend fun visitNullLiteral(ctx: NullLiteralContext): TShellValue = TShellValue.TNull

    @Suppress("unused")
    suspend fun visitIdentifierExpr(ctx: IdentifierExprContext): TShellValue {
      val name = ctx.IDENTIFIER()?.text ?: ctx.FUNCTION()?.text
        ?: throw TShellError.runtime("Expected identifier")
      env.get(name)?.let { return it }
      resolveCommand(name)?.let { return it }
      throw TShellError.unknownCommand(name, env.allBindings().keys + commands.names())
    }

    private fun resolveCommand(name: String): TShellValue.TFunction? {
      commandFnCache[name]?.let { return it }
      val cmd = commands.get(name) ?: return null
      val fn = TShellValue.TFunction(
        name = cmd.name,
        params = emptyList(),
        body = TShellValue.FunctionBody.Native(cmd.fn)
      )
      commandFnCache[name] = fn
      return fn
    }

    @Suppress("unused")
    suspend fun visitArrayExpr(ctx: ArrayExprContext): TShellValue =
      eval(ctx.arrayLiteral())

    suspend fun visitArrayLiteral(ctx: ArrayLiteralContext): TShellValue {
      val elements = mutableListOf<TShellValue>()
      for (soe in ctx.spreadOrExpr()) {
        val value = eval(soe.expression())
        if (soe.SPREAD() != null) {
          when (value) {
            is TShellValue.TArray -> elements.addAll(value.elements)
            else -> throw TShellError.typeMismatch("spread", "array", value, "... can only spread arrays into arrays")
          }
        } else {
          elements.add(value)
        }
      }
      return TShellValue.TArray(elements)
    }

    @Suppress("unused")
    suspend fun visitObjectExpr(ctx: ObjectExprContext): TShellValue =
      eval(ctx.objectLiteral())

    suspend fun visitObjectLiteral(ctx: ObjectLiteralContext): TShellValue {
      val fields = mutableMapOf<String, TShellValue>()
      for (field in ctx.objectField()) {
        when (field) {
          is NamedFieldContext -> {
            fields[fieldNameText(field.fieldName())] = eval(field.expression())
          }
          is ShorthandFieldContext -> {
            val name = field.IDENTIFIER()?.text ?: field.FUNCTION()?.text
              ?: throw TShellError.runtime("Expected identifier in shorthand field")
            fields[name] = env.get(name)
              ?: throw TShellError.runtime("'$name' is not defined (used as shorthand in object literal)")
          }
          is SpreadFieldContext -> {
            val value = eval(field.expression())
            when (value) {
              is TShellValue.TObject -> fields.putAll(value.fields)
              else -> throw TShellError.typeMismatch("spread", "object", value, "... can only spread objects into objects")
            }
          }
          is ComputedFieldContext -> {
            val key = eval(field.expression(0))
            val keyStr = when (key) {
              is TShellValue.TString -> key.value
              else -> throw TShellError.typeMismatch("computed key", "string", key)
            }
            fields[keyStr] = eval(field.expression(1))
          }
        }
      }
      return TShellValue.TObject(fields)
    }

    @Suppress("unused")
    suspend fun visitParenExpr(ctx: ParenExprContext): TShellValue =
      eval(ctx.expression())

    // --- Arrow functions ---

    @Suppress("unused")
    suspend fun visitSingleParamArrow(ctx: SingleParamArrowContext): TShellValue {
      val paramName = ctx.IDENTIFIER()?.text ?: ctx.FUNCTION()?.text ?: throw TShellError.runtime("Expected parameter name")
      return TShellValue.TFunction(
        name = null,
        params = listOf(paramName),
        body = TShellValue.FunctionBody.Expression(ctx.expression(), env, commands, limits)
      )
    }

    @Suppress("unused")
    suspend fun visitMultiParamArrow(ctx: MultiParamArrowContext): TShellValue {
      val params = ctx.paramList()?.param()?.map { paramName(it) } ?: emptyList()
      return TShellValue.TFunction(
        name = null,
        params = params,
        body = TShellValue.FunctionBody.Expression(ctx.expression(), env, commands, limits)
      )
    }

    @Suppress("unused")
    suspend fun visitSingleParamArrowBlock(ctx: SingleParamArrowBlockContext): TShellValue {
      val paramName = ctx.IDENTIFIER()?.text ?: ctx.FUNCTION()?.text ?: throw TShellError.runtime("Expected parameter name")
      return TShellValue.TFunction(
        name = null,
        params = listOf(paramName),
        body = TShellValue.FunctionBody.Block(ctx.block(), env, commands, limits)
      )
    }

    @Suppress("unused")
    suspend fun visitMultiParamArrowBlock(ctx: MultiParamArrowBlockContext): TShellValue {
      val params = ctx.paramList()?.param()?.map { paramName(it) } ?: emptyList()
      return TShellValue.TFunction(
        name = null,
        params = params,
        body = TShellValue.FunctionBody.Block(ctx.block(), env, commands, limits)
      )
    }

    // --- Helpers ---

    private suspend fun evalCallArgs(ctx: ArgumentListContext): CallArgs {
      val positional = mutableListOf<TShellValue>()
      val named = linkedMapOf<String, TShellValue>()
      for (arg in ctx.callArg()) {
        when (arg) {
          is NamedCallArgContext -> {
            val name = arg.IDENTIFIER()?.text ?: arg.FUNCTION()?.text
              ?: throw TShellError.runtime("Expected identifier in named argument")
            val value = eval(arg.expression())
            if (named.containsKey(name)) {
              throw TShellError.runtime("Duplicate named argument: $name")
            }
            named[name] = value
          }
          is PositionalCallArgContext -> {
            if (named.isNotEmpty()) {
              throw TShellError.runtime("Positional arguments cannot follow named arguments")
            }
            val soe = arg.spreadOrExpr()
            val value = eval(soe.expression())
            if (soe.SPREAD() != null) {
              when (value) {
                is TShellValue.TArray -> positional.addAll(value.elements)
                else -> throw TShellError.typeMismatch("spread", "array", value)
              }
            } else {
              positional.add(value)
            }
          }
          else -> {}
        }
      }
      return CallArgs(positional, named)
    }

    private suspend fun evalArgumentList(ctx: ArgumentListContext): List<TShellValue> {
      val callArgs = evalCallArgs(ctx)
      if (callArgs.hasNamed) {
        throw TShellError.runtime("Named arguments not supported here")
      }
      return callArgs.positional
    }

    private fun resolveNamedArgs(fn: TShellValue.TFunction, callArgs: CallArgs, ctx: ParserRuleContext): List<TShellValue> {
      if (!callArgs.hasNamed) return callArgs.positional
      if (fn.params.isEmpty()) {
        throw TShellError.runtime("Named arguments used but function '${fn.name ?: "<anonymous>"}' has no parameter names")
      }
      val result = Array<TShellValue>(fn.params.size) { TShellValue.TNull }
      // Fill positional args from left
      for ((i, value) in callArgs.positional.withIndex()) {
        if (i >= fn.params.size) {
          throw TShellError.runtime("Too many positional arguments for ${fn.name ?: "<anonymous>"}(${fn.params.joinToString(", ")})")
        }
        result[i] = value
      }
      // Fill named args by matching parameter names
      for ((name, value) in callArgs.named) {
        val idx = fn.params.indexOf(name)
        if (idx < 0) {
          throw TShellError.runtime(
            "Unknown named argument '$name' for ${fn.name ?: "<anonymous>"}(${fn.params.joinToString(", ")})"
          )
        }
        if (idx < callArgs.positional.size) {
          throw TShellError.runtime("Named argument '$name' conflicts with positional argument at position $idx")
        }
        result[idx] = value
      }
      return result.toList()
    }

    private suspend fun callFunction(fn: TShellValue.TFunction, args: List<TShellValue>, ctx: ParserRuleContext): TShellValue {
      step(ctx)
      return callFunctionInternal(fn, args)
    }

    /**
     * Dispatches function call by body type.
     * Native: calls the suspend fn directly (no runBlocking needed — we're already in a coroutine).
     * Expression/Block: evaluates AST in this Visitor's context using the function's captured env.
     */
    internal suspend fun callFunctionInternal(fn: TShellValue.TFunction, args: List<TShellValue>): TShellValue {
      return when (val body = fn.body) {
        is TShellValue.FunctionBody.Native -> body.fn(args)
        is TShellValue.FunctionBody.Expression -> {
          pushCall(body.node as ParserRuleContext)
          val fnEnv = body.capturedEnv.child()
          fn.params.forEachIndexed { i, p -> fnEnv.define(p, args.getOrElse(i) { TShellValue.TNull }) }
          val outerEnv = env
          env = fnEnv
          try {
            eval(body.node as ParserRuleContext)
          } finally {
            env = outerEnv
            popCall()
          }
        }
        is TShellValue.FunctionBody.Block -> {
          pushCall(body.node as ParserRuleContext)
          val fnEnv = body.capturedEnv.child()
          fn.params.forEachIndexed { i, p -> fnEnv.define(p, args.getOrElse(i) { TShellValue.TNull }) }
          val outerEnv = env
          env = fnEnv
          try {
            visitBlock(body.node as BlockContext)
          } catch (r: ReturnSignal) {
            r.value
          } finally {
            env = outerEnv
            popCall()
          }
        }
      }
    }

    private suspend fun bindDestructure(ctx: DestructureContext, value: TShellValue) {
      when {
        ctx.IDENTIFIER() != null -> env.define(ctx.IDENTIFIER().text, value)
        ctx.FUNCTION() != null -> env.define(ctx.FUNCTION().text, value)
        ctx.objectDestructure() != null -> {
          val obj = value as? TShellValue.TObject
            ?: throw TShellError.typeMismatch("destructure", "object", value)
          for (field in ctx.objectDestructure().destructureField()) {
            val fieldName = identOrFunctionText(field.IDENTIFIER(), field.FUNCTION())
            val targetDestructure = field.destructure()
            val fieldValue = obj.fields[fieldName]
              ?: field.expression()?.let { eval(it) }
              ?: TShellValue.TNull
            if (targetDestructure != null) {
              bindDestructure(targetDestructure, fieldValue)
            } else {
              env.define(fieldName, fieldValue)
            }
          }
        }
        ctx.arrayDestructure() != null -> {
          val arr = value as? TShellValue.TArray
            ?: throw TShellError.typeMismatch("destructure", "array", value)
          ctx.arrayDestructure().destructure().forEachIndexed { idx, dest ->
            bindDestructure(dest, arr.elements.getOrElse(idx) { TShellValue.TNull })
          }
        }
      }
    }

    /** Extract name from a token that can be IDENTIFIER or FUNCTION (fn alias). */
    private fun identOrFunctionText(ident: org.antlr.v4.runtime.tree.TerminalNode?, func: org.antlr.v4.runtime.tree.TerminalNode?): String =
      ident?.text ?: func?.text ?: throw TShellError.runtime("Expected identifier")

    private fun paramName(p: ParamContext): String = identOrFunctionText(p.IDENTIFIER(), p.FUNCTION())

    private fun fieldNameText(ctx: com.iodesystems.tshell.parser.TShellParser.FieldNameContext): String {
      val str = ctx.STRING()
      if (str != null) {
        val raw = str.text
        return unescapeString(raw.substring(1, raw.length - 1))
      }
      return ctx.text
    }

    // FieldStep/IndexStep defined at file level

    private suspend fun buildAccessorSteps(target: AssignTargetContext): List<Any> {
      val steps = mutableListOf<Any>()
      var fieldIdx = 0
      var exprIdx = 0
      for (i in 1 until target.childCount) {
        val child = target.getChild(i)
        if (child is org.antlr.v4.runtime.tree.TerminalNode) {
          when (child.symbol.type) {
            TShellLexer.DOT -> {
              steps.add(FieldStep(fieldNameText(target.fieldName(fieldIdx))))
              fieldIdx++
            }
            TShellLexer.LBRACKET -> {
              val indexValue = eval(target.expression(exprIdx))
              steps.add(IndexStep(indexValue))
              exprIdx++
            }
          }
        }
      }
      return steps
    }

    private fun resolveStep(obj: TShellValue, step: Any): TShellValue {
      return when (step) {
        is FieldStep -> accessMember(obj, step.name)
        is IndexStep -> accessIndex(obj, step.value)
        else -> throw TShellError.runtime("Unknown accessor step")
      }
    }

    private fun applyCompoundOp(op: String, current: TShellValue, rhs: TShellValue): TShellValue {
      return when (op) {
        "+=" -> add(current, rhs)
        "-=" -> numericOp(current, rhs, "-") { a, b -> a - b }
        "*=" -> numericOp(current, rhs, "*") { a, b -> a * b }
        "/=" -> numericOp(current, rhs, "/") { a, b -> a / b }
        "%=" -> numericOp(current, rhs, "%") { a, b -> a % b }
        "&=" -> intBitwiseOp(current, rhs, "&") { a, b -> a and b }
        "|=" -> intBitwiseOp(current, rhs, "|") { a, b -> a or b }
        "^=" -> intBitwiseOp(current, rhs, "^") { a, b -> a xor b }
        "<<=" -> intBitwiseOp(current, rhs, "<<") { a, b -> a shl (b and 0x1f) }
        ">>=" -> intBitwiseOp(current, rhs, ">>") { a, b -> a shr (b and 0x1f) }
        ">>>=" -> intBitwiseOp(current, rhs, ">>>") { a, b -> a ushr (b and 0x1f) }
        else -> throw TShellError.runtime("Unknown assignment operator: $op")
      }
    }

    /** Extract a callable TFunction from a value — supports TFunction directly and TObject with __call */
    private fun asCallable(value: TShellValue): TShellValue.TFunction? = when (value) {
      is TShellValue.TFunction -> value
      is TShellValue.TObject -> value.fields["__call"] as? TShellValue.TFunction
      else -> null
    }

    private fun accessMember(obj: TShellValue, field: String): TShellValue {
      return when (obj) {
        is TShellValue.TObject -> when (field) {
          "hasOwnProperty" -> TShellValue.TFunction(
            name = "hasOwnProperty",
            params = listOf("key"),
            body = TShellValue.FunctionBody.Native { args ->
              val key = (args.firstOrNull() as? TShellValue.TString)?.value
                ?: throw TShellError.typeMismatch("hasOwnProperty", "string", args.firstOrNull() ?: TShellValue.TNull)
              TShellValue.TBoolean(obj.fields.containsKey(key))
            }
          )
          else -> obj.fields[field] ?: TShellValue.TNull
        }
        is TShellValue.TArray -> when (field) {
          "length" -> TShellValue.TNumber(obj.elements.size.toDouble())
          else -> bindMethodOrHint(obj, "array", field)
        }
        is TShellValue.TString -> when (field) {
          "length" -> TShellValue.TNumber(obj.value.length.toDouble())
          else -> bindMethodOrHint(obj, "string", field)
        }
        else -> throw TShellError.typeMismatch(
          "member access .$field",
          "object, array, or string",
          obj
        )
      }
    }

    /**
     * Method syntax sugar: `receiver.commandName(args)` → `commandName(receiver, args)`
     */
    private fun bindMethodOrHint(receiver: TShellValue, type: String, field: String): TShellValue {
      val cmdName = JS_METHOD_ALIASES[type]?.get(field) ?: field
      commands.get(cmdName)?.let { cmd ->
        return TShellValue.TFunction(
          name = cmdName,
          params = emptyList(),
          body = TShellValue.FunctionBody.Native { args ->
            cmd.fn(listOf(receiver) + args)
          }
        )
      }
      JS_METHOD_HINTS[type]?.get(field)?.let { hint ->
        throw TShellError("'$field' is not available as a method — $hint")
      }
      return TShellValue.TNull
    }

    private fun accessIndex(obj: TShellValue, index: TShellValue): TShellValue {
      return when (obj) {
        is TShellValue.TArray -> {
          val idx = (index as? TShellValue.TNumber)?.value?.toInt()
            ?: throw TShellError.typeMismatch("index", "number", index)
          obj.elements.getOrElse(idx) { TShellValue.TNull }
        }
        is TShellValue.TObject -> {
          val key = when (index) {
            is TShellValue.TString -> index.value
            is TShellValue.TNumber -> index.toDisplayString()
            else -> throw TShellError.typeMismatch("index", "string", index)
          }
          obj.fields[key] ?: TShellValue.TNull
        }
        is TShellValue.TString -> {
          val idx = (index as? TShellValue.TNumber)?.value?.toInt()
            ?: throw TShellError.typeMismatch("index", "number", index)
          if (idx in obj.value.indices) TShellValue.TString(obj.value[idx].toString()) else TShellValue.TNull
        }
        else -> throw TShellError.typeMismatch("index access", "array, object, or string", obj)
      }
    }

    private fun add(left: TShellValue, right: TShellValue): TShellValue {
      // String concatenation if either is a string
      if (left is TShellValue.TString || right is TShellValue.TString) {
        return TShellValue.TString(left.toDisplayString() + right.toDisplayString())
      }
      return numericOp(left, right, "+") { a, b -> a + b }
    }

    private fun intBitwiseOp(
      left: TShellValue,
      right: TShellValue,
      op: String,
      fn: (Int, Int) -> Int
    ): TShellValue {
      val l = (left as? TShellValue.TNumber)
        ?: throw TShellError.typeMismatch("'$op'", "number", left)
      val r = (right as? TShellValue.TNumber)
        ?: throw TShellError.typeMismatch("'$op'", "number", right)
      return TShellValue.TNumber(fn(l.value.toInt(), r.value.toInt()).toDouble())
    }

    private fun numericOp(
      left: TShellValue,
      right: TShellValue,
      op: String,
      fn: (Double, Double) -> Double
    ): TShellValue {
      val l = (left as? TShellValue.TNumber)
        ?: throw TShellError.typeMismatch("'$op'", "number", left)
      val r = (right as? TShellValue.TNumber)
        ?: throw TShellError.typeMismatch("'$op'", "number", right)
      return TShellValue.TNumber(fn(l.value, r.value))
    }

    private fun valueEquals(a: TShellValue, b: TShellValue): Boolean {
      return when {
        a is TShellValue.TNull && b is TShellValue.TNull -> true
        a is TShellValue.TNumber && b is TShellValue.TNumber -> a.value == b.value
        a is TShellValue.TString && b is TShellValue.TString -> a.value == b.value
        a is TShellValue.TBoolean && b is TShellValue.TBoolean -> a.value == b.value
        a is TShellValue.TArray && b is TShellValue.TArray ->
          a.elements.size == b.elements.size && a.elements.zip(b.elements).all { (x, y) -> valueEquals(x, y) }
        a is TShellValue.TObject && b is TShellValue.TObject ->
          a.fields.size == b.fields.size && a.fields.all { (k, v) -> b.fields[k]?.let { valueEquals(v, it) } ?: false }
        else -> false
      }
    }

    private fun compareValues(a: TShellValue, b: TShellValue): Int {
      return when {
        a is TShellValue.TNumber && b is TShellValue.TNumber -> a.value.compareTo(b.value)
        a is TShellValue.TString && b is TShellValue.TString -> a.value.compareTo(b.value)
        else -> throw TShellError.typeMismatch(
          "comparison",
          "matching number or string types",
          a,
          "Cannot compare ${a.typeName()} with ${b.typeName()}"
        )
      }
    }

    private fun unescapeString(s: String): String {
      val sb = StringBuilder()
      var i = 0
      while (i < s.length) {
        if (s[i] == '\\' && i + 1 < s.length) {
          when (s[i + 1]) {
            'n' -> sb.append('\n')
            't' -> sb.append('\t')
            'r' -> sb.append('\r')
            '\\' -> sb.append('\\')
            '"' -> sb.append('"')
            '\'' -> sb.append('\'')
            '`' -> sb.append('`')
            '$' -> sb.append('$')
            else -> { sb.append('\\'); sb.append(s[i + 1]) }
          }
          i += 2
        } else {
          sb.append(s[i])
          i++
        }
      }
      return sb.toString()
    }

  }
}

/** JS global namespace → { jsMethod → tshellCommand } */
private val JS_NAMESPACE_ALIASES: Map<String, Map<String, String>> = mapOf(
  "JSON" to mapOf(
    "parse" to "parseJson",
    "stringify" to "toJson",
  ),
  "Math" to mapOf(
    "floor" to "floor",
    "ceil" to "ceil",
    "round" to "round",
    "abs" to "abs",
    "min" to "min",
    "max" to "max",
    "pow" to "pow",
  ),
  "Object" to mapOf(
    "keys" to "keys",
    "values" to "values",
    "entries" to "entries",
    "fromEntries" to "fromEntries",
  ),
  "console" to mapOf(
    "log" to "print",
  ),
  "Array" to mapOf(
    "isArray" to "isArray",
    "from" to "toArray",
  ),
)

/** Array methods that mutate-and-writeback via lvalue tracking */
private val MUTATING_ARRAY_METHODS = setOf("push", "pop", "shift", "unshift", "splice")

/** JS constructor-style calls → tshell command names */
private val JS_CONSTRUCTOR_ALIASES: Map<String, String> = mapOf(
  "String" to "str",
  "Number" to "num",
  "Boolean" to "bool",
  "parseInt" to "num",
  "parseFloat" to "num",
)

/** JS method names → tshell command names, keyed by receiver type */
private val JS_METHOD_ALIASES: Map<String, Map<String, String>> = mapOf(
  "array" to mapOf(
    "includes" to "contains",
  ),
  "string" to mapOf(
    "toUpperCase" to "upper",
    "toLowerCase" to "lower",
    "includes" to "contains",
    "replaceAll" to "replace",
    "matchAll" to "match",
    "search" to "test",
    "slice" to "substring",
  ),
)

/** JS methods with no tshell equivalent — error hints */
private val JS_METHOD_HINTS: Map<String, Map<String, String>> = mapOf(
  "array" to mapOf(
    "entries" to "zip(range(0, len(arr)), arr)",
  ),
  "string" to mapOf(
    "charCodeAt" to "not available in tshell",
    "codePointAt" to "not available in tshell",
    "repeat" to "not available in tshell",
    "trimStart" to "str |> trim() (only full trim)",
    "trimEnd" to "str |> trim() (only full trim)",
  ),
)
