package com.iodesystems.tshell.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.mcp.McpServer
import com.iodesystems.tshell.mcp.McpToolkit
import com.iodesystems.tshell.sql.SqlToolkit
import com.iodesystems.tshell.toolkit.CoreToolkit
import com.iodesystems.tshell.toolkit.FileToolkit
import com.iodesystems.tshell.toolkit.WebToolkit
import kotlinx.serialization.json.*
import java.io.Closeable
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path
import javax.sql.DataSource

/**
 * CLI entry point for running tshell as an MCP server.
 *
 * Usage:
 *   tshell-cli
 *   tshell-cli --files-dir ./data
 *   tshell-cli --sql-url jdbc:sqlite:app.db
 *   tshell-cli --mcp mcp-config.json
 *   tshell-cli --connect "app=python my_tools.py"
 */
class TShellCli : SuspendingCliktCommand(
  name = "tshell-cli"
) {
  // ── File system ──
  private val filesDir by option(
    "--files-dir",
    help = "Enable file toolkit rooted at this directory."
  )
  private val filesReadOnly by option(
    "--files-read-only",
    help = "File toolkit is read-only (default: writable)."
  ).flag(default = false)

  // ── SQL ──
  private val sqlUrl by option(
    "--sql-url",
    help = "JDBC URL to connect to (e.g. jdbc:sqlite:app.db, jdbc:postgresql://localhost/mydb). " +
      "Registers as 'db' namespace. Can be specified multiple times as name=url."
  ).multiple()
  private val sqlReadOnly by option(
    "--sql-read-only",
    help = "SQL connections are read-only (default: read-only)."
  ).flag("--sql-writable", default = true)

  // ── Web ──
  private val web by option(
    "--web",
    help = "Enable web toolkit (Web.fetch, Web.search, Html.*)."
  ).flag(default = false)

  // ── MCP servers ──
  private val connect by option(
    "--connect",
    help = "External MCP server: 'command args...' or 'namespace=command args...'."
  ).multiple()

  private val mcp by option(
    "--mcp",
    help = "MCP server config: file path, inline JSON, or 'name:command args...'. " +
      "JSON expects {\"mcpServers\": {\"name\": {\"command\": \"...\", \"args\": [...]}}}."
  ).multiple()

  // ── Execution limits ──
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

    val closeables = mutableListOf<Closeable>()

    // ── File toolkit ──
    if (filesDir != null) {
      val root = Path.of(filesDir!!).toAbsolutePath()
      if (!root.toFile().isDirectory) {
        throw UsageError("--files-dir '$filesDir' is not a directory")
      }
      FileToolkit(root, readOnly = filesReadOnly).install(shell)
      System.err.println("File toolkit enabled (${if (filesReadOnly) "read-only" else "read-write"}: $root)")
    }

    // ── SQL toolkit ──
    if (sqlUrl.isNotEmpty()) {
      val databases = mutableMapOf<String, SqlToolkit.DatabaseConfig>()
      for (spec in sqlUrl) {
        val (name, url) = parseSqlSpec(spec)
        databases[name] = SqlToolkit.DatabaseConfig(
          dataSource = JdbcUrlDataSource(url),
          readOnly = sqlReadOnly,
          label = name,
        )
      }
      val sqlToolkit = SqlToolkit(databases = databases)
      sqlToolkit.install(shell)
      closeables.add(sqlToolkit)
      System.err.println("SQL toolkit enabled (${if (sqlReadOnly) "read-only" else "writable"}: ${databases.keys.joinToString(", ")})")
    }

    // ── Web toolkit ──
    if (web) {
      val webToolkit = WebToolkit()
      webToolkit.install(shell)
      closeables.add(webToolkit)
      System.err.println("Web toolkit enabled (Web.*, Html.*)")
    }

    // ── MCP servers ──
    val serverConfigs = mutableMapOf<String, McpToolkit.McpServerConfig>()

    connect.forEachIndexed { index, spec ->
      val (name, config) = parseConnectSpec(spec, index)
      serverConfigs[name] = config
    }

    mcp.forEach { mcpArg ->
      serverConfigs.putAll(parseMcpConfig(mcpArg))
    }

    var mcpToolkit: McpToolkit? = null
    if (serverConfigs.isNotEmpty()) {
      mcpToolkit = McpToolkit(servers = serverConfigs)
      mcpToolkit.install(shell)
      closeables.add(mcpToolkit)
      System.err.println("Connected to ${serverConfigs.size} MCP server(s): ${serverConfigs.keys.joinToString(", ")}")
    }

    val mcpServer = McpServer(shell, maxOutputBytes = maxOutput)

    Runtime.getRuntime().addShutdownHook(Thread {
      for (c in closeables) {
        try { c.close() } catch (_: Exception) {}
      }
    })

    mcpServer.runStdio()
  }

  private fun parseSqlSpec(spec: String): Pair<String, String> {
    // name=jdbc:... or just jdbc:...
    return if (spec.contains('=') && !spec.startsWith("jdbc:")) {
      val eqIdx = spec.indexOf('=')
      spec.substring(0, eqIdx) to spec.substring(eqIdx + 1)
    } else {
      "db" to spec
    }
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

  private fun parseMcpConfig(arg: String): Map<String, McpToolkit.McpServerConfig> {
    // Format 1: name:command args...  (shorthand)
    if (!arg.trimStart().startsWith("{") && !File(arg).exists() && arg.contains(':')) {
      val colonIdx = arg.indexOf(':')
      val name = arg.substring(0, colonIdx).trim()
      val command = arg.substring(colonIdx + 1).trim().split(Regex("\\s+"))
      if (name.isNotEmpty() && command.isNotEmpty()) {
        return mapOf(name to McpToolkit.McpServerConfig(command = command, label = name))
      }
    }

    // Format 2: file path or inline JSON
    val jsonText = if (arg.trimStart().startsWith("{")) {
      arg
    } else {
      val file = File(arg)
      if (!file.exists()) {
        throw UsageError("MCP config not found: '$arg'. Expected a JSON file, inline JSON, or name:command format.")
      }
      file.readText()
    }

    val root = try {
      Json.parseToJsonElement(jsonText).jsonObject
    } catch (e: Exception) {
      throw UsageError("Invalid JSON in MCP config: ${e.message}")
    }

    val servers = root["mcpServers"]?.jsonObject
      ?: throw UsageError("Expected 'mcpServers' key in MCP config")

    val configs = mutableMapOf<String, McpToolkit.McpServerConfig>()
    for ((name, serverJson) in servers) {
      val server = serverJson.jsonObject
      val command = server["command"]?.jsonPrimitive?.content
        ?: throw UsageError("Server '$name' missing required 'command' field")
      val args = server["args"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
      val env = server["env"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()

      configs[name] = McpToolkit.McpServerConfig(
        command = listOf(command) + args,
        env = env,
        label = name,
      )
    }
    return configs
  }
}

/**
 * Minimal DataSource wrapper around a JDBC URL for CLI usage.
 * Creates a new connection per getConnection() call.
 */
private class JdbcUrlDataSource(private val url: String) : DataSource {
  override fun getConnection() = java.sql.DriverManager.getConnection(url)!!
  override fun getConnection(username: String?, password: String?) = java.sql.DriverManager.getConnection(url, username, password)!!
  override fun getLogWriter(): PrintWriter? = null
  override fun setLogWriter(out: PrintWriter?) {}
  override fun setLoginTimeout(seconds: Int) {}
  override fun getLoginTimeout(): Int = 0
  override fun getParentLogger() = throw java.sql.SQLFeatureNotSupportedException()
  override fun <T : Any?> unwrap(iface: Class<T>?) = throw java.sql.SQLException("Not a wrapper")
  override fun isWrapperFor(iface: Class<*>?) = false
}

suspend fun main(args: Array<String>) = TShellCli().main(args)
