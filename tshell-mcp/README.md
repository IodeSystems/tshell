# tshell-mcp

MCP (Model Context Protocol) integration for tshell — both server and client.

**Server**: expose tshell as a tool to any MCP-compatible host (Claude Desktop, Cursor, etc.)
**Client**: connect to external MCP servers and use their tools as tshell commands.

## McpServer — Expose tshell as a Tool

Wraps a `TShell` instance as an MCP server with three tools:

| Tool | Description |
| --- | --- |
| `eval` | Execute tshell code. Output is truncated at a configurable limit. |
| `help` | List available commands or search for a specific one. |
| `prompt` | Get the full language reference and command signatures for system prompts. |

```kotlin
val shell = TShell()
CoreToolkit.install(shell)

val server = McpServer(shell, maxOutputBytes = 16_000)
server.runStdio() // blocks, serves over stdin/stdout
```

Pair with `tshell-cli` for a standalone MCP server with CLI options.

## McpToolkit — Connect to External MCP Servers

Connects to one or more external MCP servers via stdio, discovers their tools, and
registers them as namespaced tshell commands. The LLM sees them in `help()` automatically.

```kotlin
val toolkit = McpToolkit(
  servers = mapOf(
    "app" to McpToolkit.McpServerConfig(
      command = listOf("python", "my_tools.py"),
      env = mapOf("DB_URL" to "postgres://..."),
    ),
    "data" to McpToolkit.McpServerConfig(
      command = listOf("npx", "data-tools"),
    ),
  ),
)

toolkit.use {
  it.install(shell)

  // tshell code can now call:
  shell.eval("""app.query_users("admin") |> map(u => u.name) |> sort()""")
  shell.eval("""data.fetch("metrics") |> filter(m => m.value > 100)""")
}
```

### How it works

1. Spawns each server process and connects via MCP stdio transport
2. Calls `listTools` to discover available tools and their schemas
3. Registers each tool as `namespace.toolName(args)` in tshell
4. Arguments are converted: single object → named params, otherwise positional
5. Results are parsed back from JSON to TShellValue (fallback to string)

### Argument mapping

```javascript
// Named arguments — pass an object:
app.query({table: "users", limit: 10})

// Positional arguments — mapped to schema param names in order:
app.greet("world")
```

## TShellValueConversion

Bidirectional conversion between `TShellValue` and `kotlinx.serialization.JsonElement`:

- `toJson(value)` — TShellValue → JsonElement
- `fromJson(element)` — JsonElement → TShellValue
- `argsToJsonMap(args, paramNames)` — convert call args to MCP tool call format

## tshell-cli

The `tshell-cli` module provides a standalone MCP server binary with `--connect` for
polyglot tool composition:

```bash
# Plain tshell MCP server
tshell-cli

# Connect to external MCP servers — their tools become tshell commands
tshell-cli --connect "python my_tools.py"
tshell-cli --connect "app=python my_tools.py" --connect "data=npx data-server"

# Tune execution limits
tshell-cli --timeout 60000 --max-steps 5000000 --max-output 32000
```

The `--connect` format is `namespace=command args...` or just `command args...` (namespace
auto-derived from the command name).

## Polyglot Architecture

Write tools in any language (Go, TypeScript, Python, Rust) as MCP servers. tshell-cli
connects to all of them and presents a unified `eval` tool to the LLM:

```
LLM
 │
 ▼
tshell-cli (MCP server)
 ├── eval("app.query_users('admin') |> sort('name')")
 │    ├── tshell core (pipes, arrays, strings, math)
 │    ├── app.* → Python MCP server (your app code)
 │    └── data.* → Go MCP server (your data pipeline)
 └── help() → shows all commands from all servers
```

The LLM writes tshell code that chains tools from different servers with pipes, filters,
and transforms — all in a single `eval` call.

## Dependencies

- `tshell` — core language runtime
- `io.modelcontextprotocol:kotlin-sdk` — MCP protocol implementation
- `io.ktor:ktor-client-cio` — async HTTP client for MCP transport
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` — coroutine support
