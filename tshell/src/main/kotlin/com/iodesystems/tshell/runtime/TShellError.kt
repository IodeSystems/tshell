package com.iodesystems.tshell.runtime

class TShellError(message: String) : RuntimeException(message) {
  companion object {
    fun runtime(message: String): TShellError = TShellError(message)

    fun unknownCommand(name: String, available: Collection<String>): TShellError {
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
