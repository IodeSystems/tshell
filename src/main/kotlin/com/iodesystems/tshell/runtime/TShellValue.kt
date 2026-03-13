package com.iodesystems.tshell.runtime

import kotlinx.coroutines.runBlocking

sealed class TShellValue {
  data class TString(val value: String) : TShellValue()
  data class TNumber(val value: Double) : TShellValue()
  data class TBoolean(val value: Boolean) : TShellValue()
  data class TArray(val elements: List<TShellValue>) : TShellValue()
  data class TObject(val fields: Map<String, TShellValue>) : TShellValue()
  data object TNull : TShellValue()

  data class TFunction(
    val name: String?,
    val params: List<String>,
    val body: FunctionBody,
  ) : TShellValue() {
    /**
     * Calls this function synchronously.
     * For Native bodies: bridges suspend via runBlocking.
     * For Expression/Block bodies: creates a temp Interpreter from the stored context
     * (commands, capturedEnv, limits) and executes the AST.
     */
    fun call(args: List<TShellValue>): TShellValue = when (val b = body) {
      is FunctionBody.Native -> runBlocking { b.fn(args) }
      is FunctionBody.Expression ->
        Interpreter(b.commands, b.capturedEnv, b.limits).executeInBranch(this, args)
      is FunctionBody.Block ->
        Interpreter(b.commands, b.capturedEnv, b.limits).executeInBranch(this, args)
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
  }

  fun typeName(): String = when (this) {
    is TString -> "string"
    is TNumber -> "number"
    is TBoolean -> "boolean"
    is TArray -> "array"
    is TObject -> "object"
    is TNull -> "null"
    is TFunction -> "function"
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
  }

  fun toInspectString(): String = when (this) {
    is TString -> "\"$value\""
    else -> toDisplayString()
  }
}
