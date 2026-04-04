package com.iodesystems.tshell.runtime

sealed class TShellValue {
  data class TString(val value: String) : TShellValue()
  data class TNumber(val value: Double) : TShellValue()
  data class TBoolean(val value: Boolean) : TShellValue()
  data class TArray(val elements: List<TShellValue>) : TShellValue()
  data class TObject(val fields: Map<String, TShellValue>) : TShellValue()
  data class TRegex(val pattern: String, val flags: String = "") : TShellValue()
  data object TNull : TShellValue()

  data class TFunction(
    val name: String?,
    val params: List<String>,
    val body: FunctionBody,
    /** Default value AST nodes per param (null = no default). Same length as params. */
    val paramDefaults: List<Any?> = emptyList(),
    /** Full ParamContext AST nodes for destructured params. Same length as params. */
    val paramNodes: List<Any?> = emptyList(),
  ) : TShellValue() {
    /** Suspend-capable call — use this from coroutine contexts (native commands, etc.) */
    suspend fun callAsync(args: List<TShellValue>): TShellValue = when (val b = body) {
      is FunctionBody.Native -> b.fn(args)
      is FunctionBody.Expression ->
        Interpreter(b.commands, b.capturedEnv, b.limits).executeInBranch(this, args)
      is FunctionBody.Block ->
        Interpreter(b.commands, b.capturedEnv, b.limits).executeInBranch(this, args)
    }

    /** Synchronous bridge for external callers not in a coroutine context. */
    fun call(args: List<TShellValue>): TShellValue = kotlinx.coroutines.runBlocking {
      callAsync(args)
    }
  }

  sealed class FunctionBody {
    /** Native command or pipe function. Suspend-capable for async I/O. */
    class Native(val fn: suspend (List<TShellValue>) -> TShellValue) : FunctionBody()

    /** Arrow expression: x => expr. Stores AST node + captured env + execution context. */
    class Expression(
      val node: Any, // ExpressionContext
      val capturedEnv: Environment,
      val commands: CommandRegistry,
      val limits: ExecutionLimits
    ) : FunctionBody()

    /** Named function or arrow block: fn f() { block } or x => { block }. */
    class Block(
      val node: Any, // BlockContext
      val capturedEnv: Environment,
      val commands: CommandRegistry,
      val limits: ExecutionLimits
    ) : FunctionBody()
  }

  fun isTruthy(): Boolean = when (this) {
    is TNull -> false
    is TBoolean -> value
    is TNumber -> value != 0.0
    is TString -> value.isNotEmpty()
    is TArray -> elements.isNotEmpty()
    is TObject -> fields.isNotEmpty()
    is TFunction -> true
    is TRegex -> true
  }

  fun typeName(): String = when (this) {
    is TString -> "string"
    is TNumber -> "number"
    is TBoolean -> "boolean"
    is TArray -> "array"
    is TObject -> "object"
    is TNull -> "null"
    is TFunction -> "function"
    is TRegex -> "regex"
  }

  fun toDisplayString(): String = when (this) {
    is TNull -> "null"
    is TBoolean -> value.toString()
    is TNumber -> {
      if (value % 1.0 == 0.0 && value.isFinite()) {
        java.math.BigDecimal(value).toBigInteger().toString()
      } else {
        value.toString()
      }
    }
    is TString -> value
    is TArray -> "[${elements.joinToString(", ") { it.toInspectString() }}]"
    is TObject -> "{${fields.entries.joinToString(", ") { "${it.key}: ${it.value.toInspectString()}" }}}"
    is TFunction -> "function ${name ?: "<anonymous>"}(${params.joinToString(", ")})"
    is TRegex -> "/${pattern}/${flags}"
  }

  fun toInspectString(): String = when (this) {
    is TString -> "\"$value\""
    else -> toDisplayString()
  }
}
