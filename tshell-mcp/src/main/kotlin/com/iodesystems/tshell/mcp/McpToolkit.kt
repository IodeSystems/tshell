package com.iodesystems.tshell.mcp

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.*
import java.io.Closeable

/**
 * MCP client toolkit — connects to external MCP servers and registers their
 * tools as tshell commands under a namespace.
 *
 * Usage:
 * ```kotlin
 * val toolkit = McpToolkit(
 *   servers = mapOf(
 *     "app" to McpServerConfig(command = listOf("python", "my_tools.py")),
 *     "data" to McpServerConfig(command = listOf("npx", "data-tools")),
 *   )
 * )
 * toolkit.install(shell)
 * // tshell code can now call: app.query_users("admin") |> sort("name")
 * ```
 */
class McpToolkit(
  private val servers: Map<String, McpServerConfig>,
) : Closeable {

  data class McpServerConfig(
    val command: List<String>,
    val env: Map<String, String> = emptyMap(),
    val label: String? = null,
  )

  private data class ConnectedServer(
    val client: Client,
    val process: Process,
  )

  private val connections = mutableMapOf<String, ConnectedServer>()

  fun install(shell: TShell): TShell {
    for ((namespace, config) in servers) {
      installServer(shell, namespace, config)
    }
    return shell
  }

  private fun installServer(shell: TShell, namespace: String, config: McpServerConfig) {
    val connected = connect(namespace, config)

    val tools = runBlocking {
      connected.client.listTools()
    }

    if (tools.tools.isEmpty()) return

    val toolDescriptions = mutableListOf<String>()

    for (tool in tools.tools) {
      val schema = tool.inputSchema
      val paramNames = extractParamNames(schema)
      val signature = deriveSignature(schema)
      val description = tool.description ?: tool.name

      toolDescriptions.add("  ${namespace}.${tool.name}($signature) — $description")

      shell.register(
        name = tool.name,
        namespace = namespace,
        signature = signature,
        description = description,
      ) { args ->
        val jsonArgs = TShellValueConversion.argsToJsonMap(args, paramNames)
        // callTool expects Map<String, Any?> — convert JsonElement values
        val callArgs = jsonArgs.mapValues { jsonElementToAny(it.value) }
        val result = connected.client.callTool(tool.name, callArgs)
        convertCallToolResult(result)
      }
    }

    val label = config.label ?: namespace
    shell.registerGuide(namespace, buildString {
      appendLine("$label — MCP server (${config.command.joinToString(" ")})")
      appendLine()
      appendLine("Available tools:")
      for (desc in toolDescriptions) {
        appendLine(desc)
      }
    }.trimEnd())
  }

  private fun connect(namespace: String, config: McpServerConfig): ConnectedServer {
    val pb = ProcessBuilder(config.command)
    pb.environment().putAll(config.env)
    pb.redirectError(ProcessBuilder.Redirect.INHERIT)
    val process = pb.start()

    val transport = StdioClientTransport(
      input = process.inputStream.asSource().buffered(),
      output = process.outputStream.asSink().buffered(),
    )

    val client = Client(
      clientInfo = Implementation(name = "tshell-mcp", version = "0.1.0"),
    )

    runBlocking {
      client.connect(transport)
    }

    val connected = ConnectedServer(client, process)
    connections[namespace] = connected
    return connected
  }

  private fun convertCallToolResult(result: CallToolResult): TShellValue {
    if (result.isError == true) {
      val msg = result.content.filterIsInstance<TextContent>().joinToString("\n") { it.text }
      throw TShellError("MCP tool error: $msg")
    }

    val textParts = result.content.filterIsInstance<TextContent>()
    if (textParts.isEmpty()) return TNull

    val text = textParts.joinToString("\n") { it.text }

    return try {
      val element = Json.parseToJsonElement(text)
      TShellValueConversion.fromJson(element)
    } catch (_: Exception) {
      TString(text)
    }
  }

  private fun jsonElementToAny(element: JsonElement): Any? = when (element) {
    is JsonNull -> null
    is JsonPrimitive -> when {
      element.isString -> element.content
      element.content == "true" -> true
      element.content == "false" -> false
      else -> element.content.toDoubleOrNull() ?: element.content
    }
    is JsonArray -> element.map { jsonElementToAny(it) }
    is JsonObject -> element.mapValues { jsonElementToAny(it.value) }
  }

  private fun extractParamNames(schema: ToolSchema): List<String> {
    return schema.properties?.keys?.toList() ?: emptyList()
  }

  private fun deriveSignature(schema: ToolSchema): String {
    val props = schema.properties ?: return ""
    val required = schema.required?.toSet() ?: emptySet()

    return props.entries.joinToString(", ") { (name, propSchema) ->
      val type = (propSchema as? JsonObject)?.get("type")?.let {
        (it as? JsonPrimitive)?.content
      } ?: "any"
      val opt = if (name !in required) "?" else ""
      "$name$opt: $type"
    }
  }

  override fun close() {
    for ((_, conn) in connections) {
      try {
        runBlocking { conn.client.close() }
      } catch (_: Exception) {}
      try {
        conn.process.destroy()
        if (!conn.process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
          conn.process.destroyForcibly()
        }
      } catch (_: Exception) {}
    }
    connections.clear()
  }
}
