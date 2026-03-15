# tshell-mcp

MCP server and client library for tshell. Exposes tshell as an MCP server (`McpServer`)
and connects to external MCP servers as namespaced commands (`McpToolkit`).

For the standalone CLI binary, see [`tshell-cli/README.md`](../tshell-cli/README.md).

## McpServer — expose tshell as an MCP server

Wraps a `TShell` instance as an MCP server with three tools: `eval`, `help`, `prompt`.

```kotlin
val shell = TShell()
CoreToolkit.install(shell)

val server = McpServer(shell, maxOutputBytes = 16_000)
server.runStdio()
```

| Tool | Purpose |
| --- | --- |
| `eval` | Execute tshell code. JS-syntax with pipes; results returned as text. |
| `help` | Runtime discovery. Searchable, shows examples and guides. |
| `prompt` | Compact language reference + all command signatures (~1460 tokens). Optimized for system prompt context budget. |

## McpToolkit — connect to external MCP servers

Spawns MCP server processes, discovers their tools via `listTools`, and registers
them as namespaced tshell commands. The LLM sees them in `help()` automatically.

```kotlin
val shell = TShell()
CoreToolkit.install(shell)

val toolkit = McpToolkit(
  servers = mapOf(
    "app" to McpToolkit.McpServerConfig(
      command = listOf("python", "my_tools.py"),
      env = mapOf("DB_URL" to "postgres://..."),
    ),
    "browser" to McpToolkit.McpServerConfig(
      command = listOf("npx", "@anthropic/mcp-playwright"),
    ),
  ),
)
toolkit.install(shell)

// tshell code can now call across servers:
shell.eval("""app.query_users("admin") |> map(u => u.name) |> sort()""")
shell.eval("""browser.navigate("https://example.com") |> querySelectorAll("a")""")

toolkit.close()
```

### Argument mapping

```javascript
// Named arguments — pass an object:
app.query({table: "users", limit: 10})

// Positional — mapped to schema param names in order:
app.greet("world")
```

## Dependencies

- `tshell` — core language runtime
- `io.modelcontextprotocol:kotlin-sdk` — MCP protocol implementation
- `io.ktor:ktor-client-cio` — async HTTP client for MCP transport
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` — coroutine support
