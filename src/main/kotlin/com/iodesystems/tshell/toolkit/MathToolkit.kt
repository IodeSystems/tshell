package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import kotlin.math.pow

/**
 * JS-style Math namespace for tshell.
 *
 * Installs a global object (default name "Math") with functions like
 * `Math.sqrt(5)`, `Math.PI`, `Math.sin(x)`, etc.
 */
class MathToolkit(
  private val globalName: String = "Math"
) {
  fun install(shell: TShell): TShell {
    val fields = mutableMapOf<String, TShellValue>()

    // --- Constants ---
    fields["PI"] = TNumber(Math.PI)
    fields["E"] = TNumber(Math.E)
    fields["LN2"] = TNumber(kotlin.math.ln(2.0))
    fields["LN10"] = TNumber(kotlin.math.ln(10.0))
    fields["SQRT2"] = TNumber(kotlin.math.sqrt(2.0))

    // --- Unary math functions ---
    fun unaryFn(name: String, op: (Double) -> Double): TFunction {
      return TFunction(name, listOf("n"), FunctionBody.Native { args ->
        val n = (args.firstOrNull() as? TNumber)?.value
          ?: throw TShellError.typeMismatch("$globalName.$name", "number", args.firstOrNull() ?: TNull)
        TNumber(op(n))
      })
    }

    fields["abs"] = unaryFn("abs") { kotlin.math.abs(it) }
    fields["floor"] = unaryFn("floor") { kotlin.math.floor(it) }
    fields["ceil"] = unaryFn("ceil") { kotlin.math.ceil(it) }
    @Suppress("RedundantCallOfConversionMethod")
    fields["round"] = unaryFn("round") { kotlin.math.round(it).toDouble() }
    fields["sign"] = unaryFn("sign") { kotlin.math.sign(it) }
    fields["trunc"] = unaryFn("trunc") { if (it >= 0) kotlin.math.floor(it) else kotlin.math.ceil(it) }
    fields["sqrt"] = unaryFn("sqrt") { kotlin.math.sqrt(it) }
    fields["cbrt"] = unaryFn("cbrt") { Math.cbrt(it) }
    fields["exp"] = unaryFn("exp") { kotlin.math.exp(it) }
    fields["log"] = unaryFn("log") { kotlin.math.ln(it) }
    fields["log2"] = unaryFn("log2") { kotlin.math.log2(it) }
    fields["log10"] = unaryFn("log10") { kotlin.math.log10(it) }
    fields["sin"] = unaryFn("sin") { kotlin.math.sin(it) }
    fields["cos"] = unaryFn("cos") { kotlin.math.cos(it) }
    fields["tan"] = unaryFn("tan") { kotlin.math.tan(it) }
    fields["asin"] = unaryFn("asin") { kotlin.math.asin(it) }
    fields["acos"] = unaryFn("acos") { kotlin.math.acos(it) }
    fields["atan"] = unaryFn("atan") { kotlin.math.atan(it) }

    // --- Binary math functions ---
    fun binaryFn(name: String, op: (Double, Double) -> Double): TFunction {
      return TFunction(name, listOf("a", "b"), FunctionBody.Native { args ->
        val a = (args.getOrNull(0) as? TNumber)?.value
          ?: throw TShellError.typeMismatch("$globalName.$name", "number", args.getOrNull(0) ?: TNull)
        val b = (args.getOrNull(1) as? TNumber)?.value
          ?: throw TShellError.typeMismatch("$globalName.$name", "number", args.getOrNull(1) ?: TNull)
        TNumber(op(a, b))
      })
    }

    fields["pow"] = binaryFn("pow") { a, b -> a.pow(b) }
    fields["atan2"] = binaryFn("atan2") { y, x -> kotlin.math.atan2(y, x) }
    fields["hypot"] = binaryFn("hypot") { a, b -> kotlin.math.hypot(a, b) }

    // --- Variadic: min, max ---
    fields["min"] = TFunction("min", listOf("...values"), FunctionBody.Native { args ->
      if (args.isEmpty()) throw TShellError("$globalName.min: requires at least one argument")
      val nums = args.map {
        (it as? TNumber)?.value ?: throw TShellError.typeMismatch("$globalName.min", "number", it)
      }
      TNumber(nums.min())
    })

    fields["max"] = TFunction("max", listOf("...values"), FunctionBody.Native { args ->
      if (args.isEmpty()) throw TShellError("$globalName.max: requires at least one argument")
      val nums = args.map {
        (it as? TNumber)?.value ?: throw TShellError.typeMismatch("$globalName.max", "number", it)
      }
      TNumber(nums.max())
    })

    // --- random ---
    fields["random"] = TFunction("random", emptyList(), FunctionBody.Native { _ ->
      TNumber(Math.random())
    })

    // Bind the object as a global
    shell.setState(mapOf(globalName to TObject(fields)))

    // Register a guide so help("Math") works
    shell.registerGuide(globalName, buildGuide())

    return shell
  }

  private fun buildGuide(): String = """
$globalName — JavaScript-style math namespace

Constants:
  $globalName.PI       → 3.141592653589793
  $globalName.E        → 2.718281828459045
  $globalName.LN2      → 0.6931471805599453
  $globalName.LN10     → 2.302585092994046
  $globalName.SQRT2    → 1.4142135623730951

Functions:
  $globalName.abs(n)       absolute value
  $globalName.floor(n)     round down
  $globalName.ceil(n)      round up
  $globalName.round(n)     round to nearest integer
  $globalName.sign(n)      -1, 0, or 1
  $globalName.trunc(n)     integer part (toward zero)
  $globalName.sqrt(n)      square root
  $globalName.cbrt(n)      cube root
  $globalName.exp(n)       e^n
  $globalName.log(n)       natural log (ln)
  $globalName.log2(n)      base-2 log
  $globalName.log10(n)     base-10 log
  $globalName.sin(n)       sine (radians)
  $globalName.cos(n)       cosine (radians)
  $globalName.tan(n)       tangent (radians)
  $globalName.asin(n)      arc sine
  $globalName.acos(n)      arc cosine
  $globalName.atan(n)      arc tangent
  $globalName.pow(a, b)    a raised to power b
  $globalName.atan2(y, x)  two-argument arc tangent
  $globalName.hypot(a, b)  sqrt(a² + b²)
  $globalName.min(a, b, …) minimum of arguments
  $globalName.max(a, b, …) maximum of arguments
  $globalName.random()     random number in [0, 1)

TYPICAL: Basic math
  $globalName.sqrt(25)                  // → 5
  $globalName.pow(2, 10)                // → 1024
  $globalName.floor(3.7)                // → 3
  $globalName.round($globalName.PI * 100) / 100  // → 3.14

TYPICAL: Trigonometry
  $globalName.sin($globalName.PI / 2)     // → 1
  $globalName.atan2(1, 1)                 // → 0.785... (π/4)
""".trimIndent()
}
