package com.iodesystems.tshell.runtime

class CommandRegistry {
  data class CommandDef(
    val name: String,
    val namespace: String?,
    val signature: String,
    val description: String,
    val examples: List<String> = emptyList(),
    val hidden: Boolean = false,
    val fn: suspend (List<TShellValue>) -> TShellValue
  ) {
    /** Display name: "Ns.cmd" if namespaced, "cmd" if not */
    val displayName: String get() = if (namespace != null) "$namespace.$name" else name
  }

  private val commands = mutableMapOf<String, CommandDef>()
  private val guides = mutableMapOf<String, String>()

  // Track which commands belong to each namespace
  private val namespaces = mutableMapOf<String, MutableList<CommandDef>>()

  fun register(
    name: String,
    signature: String,
    description: String,
    examples: List<String> = emptyList(),
    hidden: Boolean = false,
    namespace: String? = null,
    fn: suspend (List<TShellValue>) -> TShellValue
  ) {
    val key = if (namespace != null) "$namespace.$name" else name
    val def = CommandDef(name, namespace, signature, description, examples, hidden, fn)
    commands[key] = def
    if (namespace != null) {
      namespaces.getOrPut(namespace) { mutableListOf() }.add(def)
    }
  }

  fun registerGuide(name: String, content: String) {
    guides[name] = content
  }

  fun get(name: String): CommandDef? = commands[name]

  fun names(): Set<String> = commands.keys

  /** Returns all namespace names that have been registered */
  fun namespaceNames(): Set<String> = namespaces.keys

  /** Returns all commands in a namespace */
  fun getNamespaceCommands(namespace: String): List<CommandDef> =
    namespaces[namespace] ?: emptyList()

  /**
   * Builds the namespace object (TObject with TFunction fields) for a given namespace.
   * Called by TShell after registration to populate the environment.
   */
  fun buildNamespaceObject(namespace: String): TShellValue.TObject? {
    val cmds = namespaces[namespace] ?: return null
    val fields = cmds.associate { cmd ->
      cmd.name to TShellValue.TFunction(
        name = cmd.displayName,
        params = emptyList(),
        body = TShellValue.FunctionBody.Native(cmd.fn)
      ) as TShellValue
    }
    return TShellValue.TObject(fields)
  }

  fun toHelp(search: String?): String {
    if (search == null) {
      // No search: list all visible commands, grouped by namespace
      val visible = commands.values.filter { !it.hidden }
      if (visible.isEmpty()) return "No commands available."
      val sb = StringBuilder()

      // Global commands first
      val globals = visible.filter { it.namespace == null }.sortedBy { it.name }
      if (globals.isNotEmpty()) {
        sb.appendLine(globals.joinToString("\n") { cmd ->
          "  ${cmd.name}(${cmd.signature}) — ${cmd.description}"
        })
      }

      // Namespaced commands grouped
      val nsByName = visible.filter { it.namespace != null }.groupBy { it.namespace!! }
      for ((ns, cmds) in nsByName.entries.sortedBy { it.key }) {
        if (globals.isNotEmpty() || nsByName.keys.first() != ns) sb.appendLine()
        sb.appendLine("  $ns:")
        for (cmd in cmds.sortedBy { it.name }) {
          sb.appendLine("    $ns.${cmd.name}(${cmd.signature}) — ${cmd.description}")
        }
      }

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

    // Exact name match (supports both "cmd" and "Ns.cmd")
    val exact = commands[search]
    if (exact != null) return detailedHelp(exact)

    // Check if search matches a namespace — list all commands in it
    val nsCmds = namespaces[search]
    if (nsCmds != null) {
      return "$search:\n\n" + nsCmds.sortedBy { it.name }.joinToString("\n") { cmd ->
        "  $search.${cmd.name}(${cmd.signature}) — ${cmd.description}"
      } + "\n\n  Use help(\"$search.commandName\") for detailed help."
    }

    // Fuzzy search: match name, displayName, description, signature
    val q = search.lowercase()
    val cmds = commands.values.filter {
      it.name.lowercase().contains(q) ||
        it.displayName.lowercase().contains(q) ||
        it.description.lowercase().contains(q) ||
        it.signature.lowercase().contains(q)
    }

    if (cmds.isEmpty()) {
      val allNames = commands.keys + namespaces.keys
      val similar = allNames
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

    if (cmds.size == 1) return detailedHelp(cmds[0])

    return "Commands matching '$search':\n\n" + cmds.sortedBy { it.displayName }.joinToString("\n") { cmd ->
      "  ${cmd.displayName}(${cmd.signature}) — ${cmd.description}"
    } + "\n\n  Use help(\"commandName\") for detailed help on a specific command."
  }

  private fun detailedHelp(cmd: CommandDef): String {
    val sb = StringBuilder()
    sb.appendLine("${cmd.displayName}(${cmd.signature})")
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
  fun toPrompt(): String {
    val visible = commands.values.filter { !it.hidden }
    if (visible.isEmpty()) return "No commands available."
    return visible.sortedBy { it.displayName }.joinToString("\n") { cmd ->
      val compactSig = cmd.signature
        .replace(Regex("""^input:\s*\w+(\|\w+)*,\s*"""), "")
        .replace(Regex("""^input:\s*\w+(\|\w+)*$"""), "")
      "  ${cmd.displayName}($compactSig) — ${cmd.description}"
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
