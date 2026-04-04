package com.iodesystems.tshell.mcp

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellValue
import io.modelcontextprotocol.kotlin.sdk.server.ClientConnection
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.*

private fun jsonToTShellValue(element: JsonElement): TShellValue = when (element) {
  is JsonPrimitive -> when {
    element.isString -> TShellValue.TString(element.content)
    element.content == "true" -> TShellValue.TBoolean(true)
    element.content == "false" -> TShellValue.TBoolean(false)
    element.content == "null" -> TShellValue.TNull
    else -> TShellValue.TNumber(element.content.toDouble())
  }
  is JsonArray -> TShellValue.TArray(element.map { jsonToTShellValue(it) })
  is JsonObject -> TShellValue.TObject(element.mapValues { (_, v) -> jsonToTShellValue(v) })
}

class McpServer(
  private val shell: TShell,
  private val maxOutputBytes: Int = 16_000,
) {

  fun createServer(): Server {
    val server = Server(
      Implementation(name = "tshell", version = "0.1.0"),
      ServerOptions(
        capabilities = ServerCapabilities(
          tools = ServerCapabilities.Tools(listChanged = false),
        ),
      ),
    )

    val evalHandler: suspend (ClientConnection, CallToolRequest) -> CallToolResult = { _, request ->
      val code = request.arguments?.get("code")?.jsonPrimitive?.content
      if (code == null) {
        CallToolResult(
          content = listOf(TextContent(text = "ERROR: missing 'code' argument")),
          isError = true,
        )
      } else {
        try {
          val vars = request.arguments?.get("vars")?.jsonObject?.let { jsonObj ->
            jsonObj.mapValues { (_, v) -> jsonToTShellValue(v) }
          } ?: emptyMap()
          val raw = shell.evalExported(code, vars).toDisplayString()
          val output = if (raw.length > maxOutputBytes) {
            raw.substring(0, maxOutputBytes) +
              "\n\n... OUTPUT TRUNCATED (${raw.length} bytes, limit $maxOutputBytes). " +
              "Use limit(), filter(), or read(path, start, lines) to reduce output."
          } else raw
          CallToolResult(content = listOf(TextContent(text = output)))
        } catch (e: Exception) {
          CallToolResult(
            content = listOf(TextContent(text = "ERROR: ${e.message}")),
            isError = true,
          )
        }
      }
    }

    server.addTool(
      Tool(
        name = "eval",
        description = TShell.TOOL_DESCRIPTION,
        inputSchema = ToolSchema(
          properties = JsonObject(mapOf(
            "code" to JsonObject(mapOf(
              "type" to JsonPrimitive("string"),
              "description" to JsonPrimitive("tshell source code"),
            )),
            "vars" to JsonObject(mapOf(
              "type" to JsonPrimitive("object"),
              "description" to JsonPrimitive("RECOMMENDED for file paths, regex patterns, and user data. Bound as constants before execution — avoids double-escaping errors that LLMs commonly make."),
              "additionalProperties" to JsonPrimitive(true),
            )),
          )),
          required = listOf("code"),
        ),
      ),
      evalHandler,
    )

    val helpHandler: suspend (ClientConnection, CallToolRequest) -> CallToolResult = { _, request ->
      val search = request.arguments?.get("search")?.jsonPrimitive?.contentOrNull
      val help = shell.commands.toHelp(search)
      CallToolResult(content = listOf(TextContent(text = help)))
    }

    server.addTool(
      Tool(
        name = "help",
        description = "List available tshell commands or get detailed help for a specific command",
        inputSchema = ToolSchema(
          properties = JsonObject(mapOf(
            "search" to JsonObject(mapOf(
              "type" to JsonPrimitive("string"),
              "description" to JsonPrimitive("command name to search for"),
            ))
          )),
        ),
      ),
      helpHandler,
    )

    val promptHandler: suspend (ClientConnection, CallToolRequest) -> CallToolResult = { _, request ->
      val detail = request.arguments?.get("detail")?.jsonPrimitive?.booleanOrNull ?: false
      CallToolResult(content = listOf(TextContent(text = shell.toPrompt(compact = !detail))))
    }

    server.addTool(
      Tool(
        name = "prompt",
        description = "Get tshell language reference. Default: compact (names only, use help() for details). detail=true for full signatures.",
        inputSchema = ToolSchema(
          properties = JsonObject(mapOf(
            "detail" to JsonObject(mapOf(
              "type" to JsonPrimitive("boolean"),
              "description" to JsonPrimitive("true for full signatures and descriptions; false (default) for compact listing"),
            ))
          )),
        ),
      ),
      promptHandler,
    )

    return server
  }

  suspend fun runStdio() {
    val server = createServer()
    val transport = StdioServerTransport(
      inputStream = System.`in`.asSource().buffered(),
      outputStream = System.out.asSink().buffered(),
    )
    server.createSession(transport)
    val latch = java.util.concurrent.CountDownLatch(1)
    server.onClose { latch.countDown() }
    latch.await()
  }
}
