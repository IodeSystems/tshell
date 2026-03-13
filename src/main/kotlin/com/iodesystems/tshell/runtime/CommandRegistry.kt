package com.iodesystems.tshell.runtime

class CommandRegistry {
  data class CommandDef(
    val name: String,
    val signature: String,
    val description: String,
    val examples: List<String> = emptyList(),
    val hidden: Boolean = false,
    val fn: suspend (List<TShellValue>) -> TShellValue
  )

  private val commands = mutableMapOf<String, CommandDef>()
  private val guides = mutableMapOf<String, String>()

  fun register(
    name: String,
    signature: String,
    description: String,
    examples: List<String> = emptyList(),
    hidden: Boolean = false,
    fn: suspend (List<TShellValue>) -> TShellValue
  ) {
    commands[name] = CommandDef(name, signature, description, examples, hidden, fn)
  }

  fun registerGuide(name: String, content: String) {
    guides[name] = content
  }

  fun get(name: String): CommandDef? = commands[name]

  fun names(): Set<String> = commands.keys

  fun toHelp(search: String?): String {
    if (search == null) {
      // No search: list all visible commands in compact format
      val visible = commands.values.filter { !it.hidden }
      if (visible.isEmpty()) return "No commands available."
      val sb = StringBuilder()
      sb.appendLine(visible.sortedBy { it.name }.joinToString("\n") { cmd ->
        "  ${cmd.name}(${cmd.signature}) — ${cmd.description}"
      })
      if (guides.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("Guides:")
        for (name in guides.keys.sorted()) {
          sb.appendLine("  help(\"$name\") — $name usage patterns")
        }
      }
      return sb.toString().trimEnd()
    }

    // Exact guide match
    val guide = guides[search]
    if (guide != null) return guide

    // Exact name match: show detailed help for that one command
    val exact = commands[search]
    if (exact != null) return detailedHelp(exact)

    // Fuzzy search: match name, description, signature
    val q = search.lowercase()
    val cmds = commands.values.filter {
      it.name.lowercase().contains(q) ||
        it.description.lowercase().contains(q) ||
        it.signature.lowercase().contains(q)
    }

    if (cmds.isEmpty()) {
      // No match: suggest similar names
      val similar = commands.keys
        .map { it to levenshtein(search, it) }
        .filter { it.second <= 3 }
        .sortedBy { it.second }
        .take(3)
        .map { it.first }
      val suggestion = if (similar.isNotEmpty()) {
        "\n\n  Did you mean?\n" + similar.joinToString("\n") { "    help(\"$it\")" }
      } else ""
      return "No commands matching '$search'.$suggestion\n\n  Call help() to see all commands."
    }

    // Single match from fuzzy search: show detailed
    if (cmds.size == 1) return detailedHelp(cmds[0])

    // Multiple matches: show compact list
    return "Commands matching '$search':\n\n" + cmds.sortedBy { it.name }.joinToString("\n") { cmd ->
      "  ${cmd.name}(${cmd.signature}) — ${cmd.description}"
    } + "\n\n  Use help(\"commandName\") for detailed help on a specific command."
  }

  private fun detailedHelp(cmd: CommandDef): String {
    val sb = StringBuilder()
    sb.appendLine("${cmd.name}(${cmd.signature})")
    sb.appendLine()
    sb.appendLine("  ${cmd.description}")
    if (cmd.examples.isNotEmpty()) {
      sb.appendLine()
      sb.appendLine("  Examples:")
      for (ex in cmd.examples) {
        sb.appendLine("    $ex")
      }
    }
    return sb.toString().trimEnd()
  }

  // Compact listing for system prompts (no examples, no hidden)
  // Drops the first "input: type" param since pipe usage makes it implicit
  fun toPrompt(): String {
    val visible = commands.values.filter { !it.hidden }
    if (visible.isEmpty()) return "No commands available."
    return visible.sortedBy { it.name }.joinToString("\n") { cmd ->
      val compactSig = cmd.signature
        .replace(Regex("""^input:\s*\w+(\|\w+)*,\s*"""), "")  // drop "input: type, "
        .replace(Regex("""^input:\s*\w+(\|\w+)*$"""), "")     // drop "input: type" (sole param)
      "  ${cmd.name}($compactSig) — ${cmd.description}"
    }
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
