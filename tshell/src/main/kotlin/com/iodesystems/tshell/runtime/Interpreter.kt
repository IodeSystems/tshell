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

  fun eval(source: String): EvalResult = runBlocking(Dispatchers.Default) {
    evalAsync(source)
  }

  suspend fun evalAsync(source: String): EvalResult {
    val tree = parse(source)
    val visitor = Visitor(globals)
    val value = visitor.eval(tree)
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
      throw TShellError(
        "Syntax error at line $line:$col\n\n" +
          "  $sourceLine\n" +
          "  $pointer\n\n" +
          "  $friendly"
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
      is ReturnStatementContext -> visitReturnStatement(ctx)
      is BreakStatementContext -> visitBreakStatement(ctx)
      is ContinueStatementContext -> visitContinueStatement(ctx)
      is AssignStatementContext -> visitAssignStatement(ctx)
      is IncrDecrStatementContext -> visitIncrDecrStatement(ctx)
      is ExpressionStatementContext -> visitExpressionStatement(ctx)
      is IfStatementContext -> visitIfStatement(ctx)
      is WhileStatementContext -> visitWhileStatement(ctx)
      is ForOfStatementContext -> visitForOfStatement(ctx)
      is ForStatementContext -> visitForStatement(ctx)
      is BlockContext -> visitBlock(ctx)
      is BlockOrStatementContext -> visitBlockOrStatement(ctx)
      // Expressions
      is ExpressionContext -> visitExpression(ctx)
      is TernaryExprContext -> visitTernaryExpr(ctx)
      is NullCoalesceExprContext -> visitNullCoalesceExpr(ctx)
      is OrExprContext -> visitOrExpr(ctx)
      is AndExprContext -> visitAndExpr(ctx)
      is EqualityExprContext -> visitEqualityExpr(ctx)
      is ComparisonExprContext -> visitComparisonExpr(ctx)
      is PipeExprContext -> visitPipeExpr(ctx)
      is AdditiveExprContext -> visitAdditiveExpr(ctx)
      is MultiplicativeExprContext -> visitMultiplicativeExpr(ctx)
      is UnaryExprContext -> visitUnaryExpr(ctx)
      is PostfixExprContext -> visitPostfixExpr(ctx)
      // Primary expressions
      is NumberLiteralContext -> visitNumberLiteral(ctx)
      is StringLiteralContext -> visitStringLiteral(ctx)
      is TemplateLiteralContext -> visitTemplateLiteral(ctx)
      is TrueLiteralContext -> visitTrueLiteral(ctx)
      is FalseLiteralContext -> visitFalseLiteral(ctx)
      is NullLiteralContext -> visitNullLiteral(ctx)
      is IdentifierExprContext -> visitIdentifierExpr(ctx)
      is RegexExprContext -> visitRegexExpr(ctx)
      is ArrayExprContext -> visitArrayExpr(ctx)
      is ObjectExprContext -> visitObjectExpr(ctx)
      is ParenExprContext -> visitParenExpr(ctx)
      is ChainExprContext -> visitChainExpr(ctx)
      is AllExprContext -> visitAllExpr(ctx)
      is RaceExprContext -> visitRaceExpr(ctx)
      is AnyExprContext -> visitAnyExpr(ctx)
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
        ctx.letDecl() != null -> collectDestructureNames(ctx.letDecl().destructure(), exportedNames)
        ctx.fnDecl() != null -> exportedNames.add(ctx.fnDecl().IDENTIFIER().text)
        ctx.assignStatement() != null -> exportedNames.add(ctx.assignStatement().assignTarget().IDENTIFIER().text)
      }
      return result
    }

    private fun collectDestructureNames(ctx: DestructureContext, names: MutableSet<String>) {
      when {
        ctx.IDENTIFIER() != null -> names.add(ctx.IDENTIFIER().text)
        ctx.objectDestructure() != null -> {
          for (field in ctx.objectDestructure().destructureField()) {
            val targetDestructure = field.destructure()
            if (targetDestructure != null) {
              collectDestructureNames(targetDestructure, names)
            } else {
              names.add(field.IDENTIFIER().text)
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
      val value = eval(ctx.expression())
      bindDestructure(ctx.destructure(), value)
      return TShellValue.TNull
    }

    suspend fun visitFnDecl(ctx: FnDeclContext): TShellValue {
      val name = ctx.IDENTIFIER().text
      val params = ctx.paramList()?.param()?.map { it.IDENTIFIER().text } ?: emptyList()
      val capturedEnv = env
      val fn = TShellValue.TFunction(
        name = name,
        params = params,
        body = TShellValue.FunctionBody.Block(ctx.block(), capturedEnv, commands, limits)
      )
      env.defineOrSet(name, fn)
      return TShellValue.TNull
    }

    suspend fun visitReturnStatement(ctx: ReturnStatementContext): TShellValue {
      val value = if (ctx.expression() != null) eval(ctx.expression()) else TShellValue.TNull
      throw ReturnSignal(value)
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
      val rootName = target.IDENTIFIER().text
      val steps = buildAccessorSteps(target)

      if (steps.isEmpty()) {
        val finalValue = if (op == "=") rhs else {
          val current = env.get(rootName) ?: throw TShellError.runtime("'$rootName' is not defined")
          applyCompoundOp(op, current, rhs)
        }
        env.set(rootName, finalValue)
      } else {
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
        when (lastStep) {
          is FieldStep -> {
            val obj = current as? TShellValue.TObject
              ?: throw TShellError.typeMismatch("assignment to .${lastStep.name}", "object", current)
            (obj.fields as MutableMap)[lastStep.name] = finalValue
          }
          is IndexStep -> {
            when (current) {
              is TShellValue.TObject -> {
                val key = (lastStep.value as? TShellValue.TString)?.value
                  ?: throw TShellError.typeMismatch("index assignment", "string key", lastStep.value)
                (current.fields as MutableMap)[key] = finalValue
              }
              is TShellValue.TArray -> {
                val idx = (lastStep.value as? TShellValue.TNumber)?.value?.toInt()
                  ?: throw TShellError.typeMismatch("index assignment", "number", lastStep.value)
                if (idx < 0 || idx >= current.elements.size) {
                  throw TShellError.runtime("Index $idx out of bounds (size ${current.elements.size})")
                }
                (current.elements as MutableList)[idx] = finalValue
              }
              else -> throw TShellError.typeMismatch("assignment", "object or array", current)
            }
          }
          else -> throw TShellError.runtime("Unknown accessor step")
        }
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

    suspend fun visitForStatement(ctx: ForStatementContext): TShellValue {
      val outerEnv = env
      env = env.child()
      // Init
      val letInit = ctx.forInitLet()
      val assignInit = ctx.forInitAssign()
      if (letInit != null) {
        val value = eval(letInit.expression())
        env.define(letInit.IDENTIFIER().text, value)
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
            op.fieldName() != null -> accessMember(base, op.fieldName().text)
            op.LBRACKET() != null -> accessIndex(base, eval(op.expression()))
            op.LPAREN() != null -> {
              val callArgs = op.argumentList()?.let { evalCallArgs(it) } ?: CallArgs.EMPTY
              val fn = base as? TShellValue.TFunction
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
      var result = eval(ctx.equalityExpr(0))
      for (i in 1 until ctx.equalityExpr().size) {
        if (!result.isTruthy()) return result
        result = eval(ctx.equalityExpr(i))
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
      var result = eval(ctx.pipeExpr(0))
      for (i in 1 until ctx.pipeExpr().size) {
        val right = eval(ctx.pipeExpr(i))
        val op = ctx.getChild(2 * i - 1).text
        val cmp = compareValues(result, right)
        result = TShellValue.TBoolean(
          when (op) {
            "<" -> cmp < 0
            ">" -> cmp > 0
            "<=" -> cmp <= 0
            ">=" -> cmp >= 0
            else -> false
          }
        )
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
      return eval(ctx.postfixExpr())
    }

    suspend fun visitPostfixExpr(ctx: PostfixExprContext): TShellValue {
      var result = eval(ctx.primaryExpr())
      for (op in ctx.postfixOp()) {
        val isOptional = op.OPTIONAL_CHAIN() != null
        if (isOptional && result is TShellValue.TNull) {
          // Short-circuit: null?.anything = null
          continue
        }

        val current = result
        result = withLocation(op) {
          when {
            op.fieldName() != null -> {
              accessMember(current, op.fieldName().text)
            }
            op.LBRACKET() != null -> {
              val index = eval(op.expression())
              accessIndex(current, index)
            }
            op.LPAREN() != null -> {
              val callArgs = op.argumentList()?.let { evalCallArgs(it) } ?: CallArgs.EMPTY
              when (current) {
                is TShellValue.TFunction -> {
                  val args = resolveNamedArgs(current, callArgs, op)
                  callFunction(current, args, op)
                }
                else -> throw TShellError.typeMismatch("call", "function", current)
              }
            }
            else -> current
          }
        }
      }
      return result
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
    suspend fun visitTrueLiteral(ctx: TrueLiteralContext): TShellValue = TShellValue.TBoolean(true)
    @Suppress("unused")
    suspend fun visitFalseLiteral(ctx: FalseLiteralContext): TShellValue = TShellValue.TBoolean(false)
    @Suppress("unused")
    suspend fun visitNullLiteral(ctx: NullLiteralContext): TShellValue = TShellValue.TNull

    @Suppress("unused")
    suspend fun visitIdentifierExpr(ctx: IdentifierExprContext): TShellValue {
      val name = ctx.IDENTIFIER().text
      // Check environment first, then commands
      env.get(name)?.let { return it }
      commands.get(name)?.let { cmd ->
        return TShellValue.TFunction(
          name = cmd.name,
          params = emptyList(),
          body = TShellValue.FunctionBody.Native(cmd.fn)
        )
      }
      throw TShellError.unknownCommand(name, env.allBindings().keys + commands.names())
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
            fields[field.fieldName().text] = eval(field.expression())
          }
          is ShorthandFieldContext -> {
            val name = field.IDENTIFIER().text
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
      val paramName = ctx.IDENTIFIER().text
      return TShellValue.TFunction(
        name = null,
        params = listOf(paramName),
        body = TShellValue.FunctionBody.Expression(ctx.expression(), env, commands, limits)
      )
    }

    @Suppress("unused")
    suspend fun visitMultiParamArrow(ctx: MultiParamArrowContext): TShellValue {
      val params = ctx.paramList()?.param()?.map { it.IDENTIFIER().text } ?: emptyList()
      return TShellValue.TFunction(
        name = null,
        params = params,
        body = TShellValue.FunctionBody.Expression(ctx.expression(), env, commands, limits)
      )
    }

    @Suppress("unused")
    suspend fun visitSingleParamArrowBlock(ctx: SingleParamArrowBlockContext): TShellValue {
      val paramName = ctx.IDENTIFIER().text
      return TShellValue.TFunction(
        name = null,
        params = listOf(paramName),
        body = TShellValue.FunctionBody.Block(ctx.block(), env, commands, limits)
      )
    }

    @Suppress("unused")
    suspend fun visitMultiParamArrowBlock(ctx: MultiParamArrowBlockContext): TShellValue {
      val params = ctx.paramList()?.param()?.map { it.IDENTIFIER().text } ?: emptyList()
      return TShellValue.TFunction(
        name = null,
        params = params,
        body = TShellValue.FunctionBody.Block(ctx.block(), env, commands, limits)
      )
    }

    // --- Composition primitives ---

    @Suppress("unused")
    suspend fun visitChainExpr(ctx: ChainExprContext): TShellValue {
      val args = evalArgumentList(ctx.argumentList())
      if (args.isEmpty()) throw TShellError.runtime("chain() requires at least one function argument")
      var result: TShellValue = TShellValue.TNull
      for ((idx, arg) in args.withIndex()) {
        when (arg) {
          is TShellValue.TFunction -> {
            result = if (idx == 0) callFunction(arg, emptyList(), ctx) else callFunction(arg, listOf(result), ctx)
          }
          else -> throw TShellError.wrongArguments(
            "chain",
            "...fns: function[]",
            args,
            "chain(() => getData(), data => transform(data), result => save(result))"
          )
        }
      }
      return result
    }

    @Suppress("unused")
    suspend fun visitAllExpr(ctx: AllExprContext): TShellValue {
      val args = evalArgumentList(ctx.argumentList())
      val fns = args.map { arg ->
        when (arg) {
          is TShellValue.TFunction -> arg
          else -> throw TShellError.wrongArguments(
            "all",
            "...producers: (() => T)[]",
            args,
            "all(() => getA(), () => getB(), () => getC())"
          )
        }
      }
      if (fns.size <= 1) {
        return TShellValue.TArray(fns.map { callFunction(it, emptyList(), ctx) })
      }

      val branchLimitsList = fns.map { limits.fork() }
      val results = runParallel(branchLimitsList) { idx ->
        val branchInterpreter = Interpreter(commands, env, branchLimitsList[idx])
        branchInterpreter.executeInBranch(fns[idx], emptyList())
      }
      return TShellValue.TArray(results)
    }

    @Suppress("unused")
    suspend fun visitRaceExpr(ctx: RaceExprContext): TShellValue {
      val args = evalArgumentList(ctx.argumentList())
      val fns = args.map { arg ->
        when (arg) {
          is TShellValue.TFunction -> arg
          else -> throw TShellError.wrongArguments(
            "race",
            "...producers: (() => T)[]",
            args,
            "race(() => tryFirst(), () => trySecond())"
          )
        }
      }
      if (fns.size <= 1) {
        if (fns.isEmpty()) throw TShellError.runtime("race() — no producers given")
        return callFunction(fns[0], emptyList(), ctx)
      }

      // Launch all branches in parallel. First to complete successfully wins;
      // remaining branches are cancelled via their forked limits.
      val branchLimitsList = fns.map { limits.fork() }
      val remainingMs = limits.timeoutMs - (System.currentTimeMillis() - limits.startTimeMs.get())
      if (remainingMs <= 0) throw TShellError.runtime("race() — timeout already exceeded")
      val channel = kotlinx.coroutines.channels.Channel<Result<TShellValue>>(fns.size)
      return coroutineScope {
        val jobs = fns.mapIndexed { idx, fn ->
          launch {
            val branchInterpreter = Interpreter(commands, env, branchLimitsList[idx])
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
        winner ?: throw TShellError.runtime(
          "race() — all ${fns.size} producers failed"
        )
      }
    }

    @Suppress("unused")
    suspend fun visitAnyExpr(ctx: AnyExprContext): TShellValue {
      val args = evalArgumentList(ctx.argumentList())
      val fns = args.map { arg ->
        when (arg) {
          is TShellValue.TFunction -> arg
          else -> throw TShellError.wrongArguments(
            "any",
            "...producers: (() => T)[]",
            args,
            "any(() => tryA(), () => tryB(), () => tryC())"
          )
        }
      }
      if (fns.isEmpty()) throw TShellError.runtime("any() — no producers given")

      // Evaluate sequentially, return first truthy non-error result
      val errors = mutableListOf<String>()
      for ((idx, fn) in fns.withIndex()) {
        try {
          val result = callFunction(fn, emptyList(), ctx)
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

    // --- Helpers ---

    /**
     * Runs N branches in parallel with timeout enforcement.
     * If the parent's remaining timeout expires, all branches are cancelled.
     */
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

    private suspend fun evalCallArgs(ctx: ArgumentListContext): CallArgs {
      val positional = mutableListOf<TShellValue>()
      val named = linkedMapOf<String, TShellValue>()
      for (arg in ctx.callArg()) {
        when (arg) {
          is NamedCallArgContext -> {
            val name = arg.IDENTIFIER().text
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
        ctx.objectDestructure() != null -> {
          val obj = value as? TShellValue.TObject
            ?: throw TShellError.typeMismatch("destructure", "object", value)
          for (field in ctx.objectDestructure().destructureField()) {
            val fieldName = field.IDENTIFIER().text
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
              steps.add(FieldStep(target.fieldName(fieldIdx).text))
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
        else -> throw TShellError.runtime("Unknown assignment operator: $op")
      }
    }

    private fun accessMember(obj: TShellValue, field: String): TShellValue {
      return when (obj) {
        is TShellValue.TObject -> obj.fields[field] ?: TShellValue.TNull
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
      // Resolve JS method aliases to tshell command names
      val cmdName = jsMethodAlias(type, field) ?: field
      // Check if a command exists with this name — bind receiver as first arg
      commands.get(cmdName)?.let { cmd ->
        return TShellValue.TFunction(
          name = cmdName,
          params = emptyList(),
          body = TShellValue.FunctionBody.Native { args ->
            cmd.fn(listOf(receiver) + args)
          }
        )
      }
      // No command — give JS-specific guidance if we recognize the method
      jsMethodHint(type, field)?.let { throw it }
      // Truly unknown
      return TShellValue.TNull
    }

    /** Maps JS method names to tshell command names where they differ */
    private fun jsMethodAlias(type: String, method: String): String? = when (type) {
      "array" -> when (method) {
        "includes" -> "contains"
        else -> null
      }
      "string" -> when (method) {
        "toUpperCase" -> "upper"
        "toLowerCase" -> "lower"
        "includes" -> "contains"
        "replaceAll" -> "replace"
        "matchAll" -> "match"
        "search" -> "test"
        "slice" -> "substring"
        "charAt" -> null // handled by str[index]
        else -> null
      }
      else -> null
    }

    /**
     * Guidance for JS methods that have NO tshell command equivalent and can't be auto-resolved.
     */
    private fun jsMethodHint(type: String, method: String): TShellError? {
      val hint = when (type) {
        "array" -> when (method) {
          "forEach" -> "use map(fn) or for (let x of arr) { ... }"
          "push" -> "[...arr, newItem] (arrays are immutable)"
          "pop", "shift", "unshift", "splice" -> "arrays are immutable; use spread [...arr] to build new arrays"
          "concat" -> "[...arr1, ...arr2]"
          "every" -> "arr |> filter(fn) |> len() == arr |> len()"
          "some" -> "arr |> find(fn) != null"
          "indexOf" -> "arr |> find(x => x == value)"
          "entries" -> "zip(range(0, len(arr)), arr)"
          "at" -> "arr[index] (negative indices not supported)"
          "flatMap" -> "arr |> map(fn) |> flat()"
          else -> null
        }
        "string" -> when (method) {
          "charAt" -> "str[index]"
          "charCodeAt", "codePointAt" -> "not available in tshell"
          "repeat" -> "not available in tshell"
          "trimStart", "trimEnd" -> "str |> trim() (only full trim)"
          "at" -> "str[index]"
          else -> null
        }
        else -> null
      }
      return hint?.let {
        TShellError("'$method' is not available as a method — $it")
      }
    }

    private fun accessIndex(obj: TShellValue, index: TShellValue): TShellValue {
      return when (obj) {
        is TShellValue.TArray -> {
          val idx = (index as? TShellValue.TNumber)?.value?.toInt()
            ?: throw TShellError.typeMismatch("index", "number", index)
          obj.elements.getOrElse(idx) { TShellValue.TNull }
        }
        is TShellValue.TObject -> {
          val key = (index as? TShellValue.TString)?.value
            ?: throw TShellError.typeMismatch("index", "string", index)
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
