package com.iodesystems.tshell.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.mcp.McpServer
import com.iodesystems.tshell.mcp.McpToolkit
import com.iodesystems.tshell.toolkit.CoreToolkit

/**
 * CLI entry point for running tshell as an MCP server.
 *
 * Usage:
 *   tshell-cli
 *   tshell-cli --connect "python my_tools.py"
 *   tshell-cli --connect "npx data-server" --connect "python ml-tools.py"
 *   tshell-cli --timeout 60000 --max-steps 5000000
 */
class TShellCli : SuspendingCliktCommand(
  name = "tshell-cli"
) {
  private val connect by option(
    "--connect",
    help = "External MCP server command to connect to (stdio). " +
      "Format: 'command args...' or 'namespace=command args...'. " +
      "Can be specified multiple times."
  ).multiple()

  private val timeout by option(
    "--timeout",
    help = "Execution timeout in milliseconds (default: 30000)"
  ).int().default(30_000)

  private val maxSteps by option(
    "--max-steps",
    help = "Maximum execution steps (default: 1000000)"
  ).int().default(1_000_000)

  private val maxOutput by option(
    "--max-output",
    help = "Maximum output bytes (default: 16000)"
  ).int().default(16_000)

  override suspend fun run() {
    val shell = TShell(
      maxSteps = maxSteps,
      timeoutMs = timeout.toLong(),
      maxOutputBytes = maxOutput * 4,
    )
    CoreToolkit.install(shell)

    var mcpToolkit: McpToolkit? = null
    if (connect.isNotEmpty()) {
      val serverConfigs = connect.mapIndexed { index, spec ->
        parseConnectSpec(spec, index)
      }.toMap()

      mcpToolkit = McpToolkit(servers = serverConfigs)
      mcpToolkit.install(shell)
      System.err.println("Connected to ${serverConfigs.size} MCP server(s): ${serverConfigs.keys.joinToString(", ")}")
    }

    val mcpServer = McpServer(shell, maxOutputBytes = maxOutput)

    Runtime.getRuntime().addShutdownHook(Thread {
      mcpToolkit?.close()
    })

    mcpServer.runStdio()
  }

  private fun parseConnectSpec(spec: String, index: Int): Pair<String, McpToolkit.McpServerConfig> {
    val parts = if (spec.contains('=') && !spec.substringBefore('=').contains(' ')) {
      val eqIdx = spec.indexOf('=')
      val namespace = spec.substring(0, eqIdx)
      val command = spec.substring(eqIdx + 1)
      namespace to command
    } else {
      val cmdParts = spec.trim().split(Regex("\\s+"))
      val cmdName = cmdParts.last()
        .substringAfterLast('/')
        .substringBeforeLast('.')
        .replace(Regex("[^a-zA-Z0-9]"), "_")
      cmdName to spec
    }

    val command = parts.second.trim().split(Regex("\\s+"))
    return parts.first to McpToolkit.McpServerConfig(command = command)
  }
}

suspend fun main(args: Array<String>) = TShellCli().main(args)
