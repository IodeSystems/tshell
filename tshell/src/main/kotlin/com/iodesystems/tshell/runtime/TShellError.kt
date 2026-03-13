package com.iodesystems.tshell.runtime

class TShellError(message: String) : RuntimeException(message) {
  companion object {
    fun runtime(message: String): TShellError = TShellError(message)

    fun unknownCommand(name: String, available: Collection<String>): TShellError {
      // Check for JS globals/keywords first — give targeted guidance
      jsGlobalHint(name)?.let { return TShellError(it) }

      val similar = available
        .map { it to levenshtein(name, it) }
        .filter { it.second <= 3 }
        .sortedBy { it.second }
        .take(3)
        .map { it.first }

      val suggestion = if (similar.isNotEmpty()) {
        "\n\n  Did you mean?\n" + similar.joinToString("\n") { "    $it" }
      } else ""

      val availableList = if (available.isNotEmpty()) {
        "\n\n  Available commands:\n" + available.sorted().joinToString("\n") { "    $it" }
      } else ""

      return TShellError("Unknown command '$name'$suggestion$availableList")
    }

    private fun jsGlobalHint(name: String): String? = when (name) {
      // JS keywords not in tshell
      "var" -> "tshell does not support 'var' — use 'let' instead\n\n  Example: let x = 5"
      "class" -> "tshell does not support classes — use objects and functions\n\n  Example: let obj = {name: \"Alice\", greet: () => \"hi\"}"
      "new" -> "tshell does not support 'new' — there are no constructors or classes"
      "import", "require" -> "tshell does not support imports — all commands are built-in or registered via toolkits"
      "async", "await" -> "tshell does not support async/await — use all() for parallel execution\n\n  Example: all(() => fetchA(), () => fetchB())"
      "try", "catch", "finally" -> "tshell does not support try/catch — errors propagate immediately; use fail() to throw"
      "throw" -> "tshell does not support 'throw' — use fail(message) instead\n\n  Example: fail(\"something went wrong\")"
      "switch" -> "tshell does not support 'switch' — use if/else chains or ternary\n\n  Example: x == 1 ? \"one\" : x == 2 ? \"two\" : \"other\""
      "delete" -> "tshell does not support 'delete' — objects are immutable; build a new object without the key\n\n  Example: obj |> entries() |> filter(e => e.key != \"unwanted\") |> reduce((o, e) => ({...o, [e.key]: e.value}), {})"
      "void" -> "tshell does not support 'void' — use null instead"
      "instanceof" -> "tshell does not support 'instanceof' — use typeof(value) to check types\n\n  Example: typeof(x) == \"array\""
      "fn" -> "tshell does not have 'fn' — use 'function' for declarations or '=>' for arrows\n\n  Example: function add(a, b) { return a + b }\n  Example: let add = (a, b) => a + b"
      "this" -> "tshell does not support 'this' — there are no classes or methods"
      "super" -> "tshell does not support 'super' — there is no inheritance"
      "yield" -> "tshell does not support generators — use arrays and pipes for data transformation"
      "with" -> "tshell does not support 'with'"
      "do" -> "tshell does not support 'do...while' — use while() instead"
      "enum" -> "tshell does not support enums — use objects as constants\n\n  Example: let Status = {OK: 0, ERR: 1}"

      // JS global objects → tshell equivalents
      "console" -> "tshell does not have 'console' — use print() instead\n\n  Example: print(\"hello\", x)"
      "JSON" -> "tshell does not have 'JSON' — use parseJson() and toJson() instead\n\n  Example: parseJson('{\"a\": 1}')\n  Example: {a: 1} |> toJson()"
      "Math" -> "tshell does not have 'Math' — math functions are top-level\n\n  Example: floor(3.7), ceil(3.2), round(3.5), abs(-5), min(1,2), max(1,2), pow(2,3)"
      "Object" -> "tshell does not have 'Object' — use keys(), values(), entries() instead\n\n  Example: {a: 1} |> keys()    // → [\"a\"]\n  Example: {a: 1} |> entries() // → [{key: \"a\", value: 1}]"
      "Array" -> "tshell does not have 'Array' — use array literals and typeof()\n\n  Example: typeof(x) == \"array\"  // instead of Array.isArray(x)\n  Example: range(0, 5)           // instead of Array.from({length: 5})"
      "String" -> "tshell does not have 'String' constructor — use str() instead\n\n  Example: str(42)  // → \"42\""
      "Number" -> "tshell does not have 'Number' constructor — use num() instead\n\n  Example: num(\"42\")  // → 42"
      "Boolean" -> "tshell does not have 'Boolean' constructor — use truthiness directly or ternary\n\n  Example: x ? true : false"
      "Promise" -> "tshell does not have Promises — use all() for parallel execution\n\n  Example: all(() => taskA(), () => taskB())"
      "setTimeout", "setInterval", "clearTimeout", "clearInterval" -> "tshell does not support timers"
      "RegExp" -> "tshell has regex literals instead\n\n  Example: /[0-9]+/g\n  Example: \"abc123\" |> match(/[0-9]+/)"
      "Map", "Set", "WeakMap", "WeakSet" -> "tshell does not have $name — use objects and arrays\n\n  Example: unique([1, 2, 2, 3])  // Set-like dedup"
      "Symbol", "Proxy", "Reflect" -> "tshell does not support $name"
      "Error", "TypeError", "RangeError" -> "tshell does not have error types — use fail(message)\n\n  Example: fail(\"invalid input\")"
      "undefined" -> "tshell does not have 'undefined' — use null instead"
      "NaN", "Infinity" -> "tshell does not have $name — use numeric checks instead"
      "parseInt", "parseFloat" -> "tshell does not have '$name' — use num() instead\n\n  Example: num(\"42\")  // → 42"
      "isNaN", "isFinite" -> "tshell does not have '$name'"
      "encodeURIComponent", "decodeURIComponent", "encodeURI", "decodeURI" -> "tshell does not have URI encoding functions"
      "atob", "btoa" -> "tshell does not have base64 functions"
      else -> null
    }

    fun wrongArguments(
      name: String,
      expectedSignature: String,
      got: List<TShellValue>,
      example: String? = null
    ): TShellError {
      val gotStr = got.joinToString(", ") { "${it.typeName()}: ${it.toInspectString()}" }
      val exampleStr = if (example != null) "\n\n  Example:\n    $example" else ""
      return TShellError(
        "Wrong arguments for '$name'\n\n" +
          "  Expected: $name($expectedSignature)\n" +
          "  Got:      $name($gotStr)$exampleStr"
      )
    }

    fun typeMismatch(
      operation: String,
      expected: String,
      got: TShellValue,
      hint: String? = null
    ): TShellError {
      val hintStr = if (hint != null) "\n\n  Hint: $hint" else ""
      return TShellError(
        "Type mismatch in $operation\n\n" +
          "  Expected: $expected\n" +
          "  Got:      ${got.typeName()} (${got.toInspectString()})$hintStr"
      )
    }

    fun pipeMismatch(
      fromCommand: String,
      toCommand: String,
      value: TShellValue,
      expectedType: String,
      hint: String? = null
    ): TShellError {
      val hintStr = if (hint != null) "\n\n  Hint: $hint" else ""
      return TShellError(
        "Type mismatch in pipe\n\n" +
          "  '$toCommand' expects $expectedType\n" +
          "  but received ${value.typeName()} from '$fromCommand'$hintStr"
      )
    }

    private fun levenshtein(a: String, b: String): Int {
      val dp = Array(a.length + 1) { IntArray(b.length + 1) }
      for (i in 0..a.length) dp[i][0] = i
      for (j in 0..b.length) dp[0][j] = j
      for (i in 1..a.length) {
        for (j in 1..b.length) {
          dp[i][j] = minOf(
            dp[i - 1][j] + 1,
            dp[i][j - 1] + 1,
            dp[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
          )
        }
      }
      return dp[a.length][b.length]
    }
  }
}
