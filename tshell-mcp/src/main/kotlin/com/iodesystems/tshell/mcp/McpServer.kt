package com.iodesystems.tshell.mcp

import com.iodesystems.tshell.TShell
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
          val raw = shell.evalExported(code).toDisplayString()
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
            ))
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

    val promptHandler: suspend (ClientConnection, CallToolRequest) -> CallToolResult = { _, _ ->
      CallToolResult(content = listOf(TextContent(text = shell.toPrompt())))
    }

    server.addTool(
      Tool(
        name = "prompt",
        description = "Get the tshell language reference and all available commands",
        inputSchema = ToolSchema(),
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
