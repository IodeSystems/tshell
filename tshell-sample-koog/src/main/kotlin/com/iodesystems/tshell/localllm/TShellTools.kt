package com.iodesystems.tshell.localllm

import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import com.iodesystems.tshell.TShell

@LLMDescription("tshell scripting language — NOT JavaScript or Kotlin. See the system prompt for syntax rules.")
class TShellTools(
  private val shell: TShell,
  private val maxOutputBytes: Int = 16_000
) : ToolSet {

  @Tool
  @LLMDescription(TShell.TOOL_DESCRIPTION)
  fun tshell(
    @LLMDescription("tshell source code") code: String
  ): String {
    val raw = try {
      shell.evalExported(code).toDisplayString()
    } catch (e: Exception) {
      "ERROR: ${e.message ?: "${e::class.simpleName}: ${e.cause?.message ?: "unexpected internal error"}"}"
    }
    if (raw.length > maxOutputBytes) {
      return raw.substring(0, maxOutputBytes) +
        "\n\n... OUTPUT TRUNCATED (${raw.length} bytes, limit $maxOutputBytes). " +
        "Use limit(), filter(), or read(path, start, lines) to reduce output."
    }
    return raw
  }
}
