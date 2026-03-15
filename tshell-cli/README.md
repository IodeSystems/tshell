# tshell-cli

One MCP server that replaces many. Connect MCP servers written in any language —
Python, Go, TypeScript, whatever — and expose them all through a single `eval` tool.

- **Programmatic reasoning** — your LLM computes instead of guessing. Pipes, filters, math, string ops in one call
- **Context reduction** — 20 tools × ~200 tokens each = 4KB of prompt. tshell replaces them with one `eval` tool (~1.5KB total). Tools are discovered via `help()` at runtime
- **Polyglot composition** — chain calls across servers in a single expression: `app.users() |> filter(u => u.active) |> browser.screenshot(u.url)`
- **Built-in toolkits** — file system, SQL, web fetch/search, Playwright — all wired up via CLI flags

## Build & Verify

```bash
./gradlew :tshell-cli:installDist
./tshell-cli/build/install/tshell-cli/bin/tshell-cli --help
```

```
Usage: tshell-cli [<options>]

Options:
  --files-dir=<text>  Enable file toolkit rooted at this directory.
  --files-read-only   File toolkit is read-only (default: writable).
  --sql-url=<text>    JDBC URL to connect to (e.g. jdbc:sqlite:app.db,
                      jdbc:postgresql://localhost/mydb). Registers as 'db'
                      namespace. Can be specified multiple times as name=url.
  --sql-read-only / --sql-writable
                      SQL connections are read-only (default: read-only).
  --web               Enable web toolkit (Web.fetch, Web.search, Html.*).
  --connect=<text>    External MCP server: 'command args...' or
                      'namespace=command args...'.
  --mcp=<text>        MCP server config: file path, inline JSON, or
                      'name:command args...'. JSON expects {"mcpServers":
                      {"name": {"command": "...", "args": [...]}}}.
  --timeout=<int>     Execution timeout in milliseconds (default: 30000)
  --max-steps=<int>   Maximum execution steps (default: 1000000)
  --max-output=<int>  Maximum output bytes (default: 16000)
  -h, --help          Show this message and exit
```

The binary is at `./tshell-cli/build/install/tshell-cli/bin/tshell-cli`. Run it with no args to start as a stdio MCP server.

## Claude Desktop Example

Wire up tshell with file access, a Postgres database, and Playwright — all behind one `eval` tool:

```json
{
  "mcpServers": {
    "tshell": {
      "command": "./tshell-cli/build/install/tshell-cli/bin/tshell-cli",
      "args": [
        "--files-dir", "./data",
        "--sql-url", "jdbc:postgresql://localhost/mydb",
        "--web",
        "--mcp", "browser:npx @anthropic/mcp-playwright"
      ],
      "env": {
        "TSHELL_CLASSPATH": "/path/to/postgresql-42.7.5.jar"
      }
    }
  }
}
```

JDBC drivers (Postgres, MySQL, etc.) aren't bundled. Set `TSHELL_CLASSPATH` to include them.
SQLite works out of the box — just use `--sql-url jdbc:sqlite:app.db`.

## `--mcp` vs `--connect`

`--mcp` accepts the standard `mcpServers` JSON format (file path or inline JSON) —
the same format Claude Desktop, Cursor, and other MCP hosts use. Each server name
becomes a tshell namespace.

```bash
# Point to an existing config file
tshell-cli --mcp mcp-config.json

# Inline JSON
tshell-cli --mcp '{"mcpServers": {"browser": {"command": "npx", "args": ["@anthropic/mcp-playwright"]}}}'

# Shorthand — name:command args...
tshell-cli --mcp "browser:npx @anthropic/mcp-playwright"
```

`--connect` is shorthand for simple cases:

```bash
# Auto-derives namespace from command name
tshell-cli --connect "npx @anthropic/mcp-playwright"

# Explicit namespace
tshell-cli --connect "app=python my_tools.py"
```

Both can be combined and repeated:

```bash
tshell-cli --mcp mcp-config.json --connect "app=python my_tools.py" --sql-url jdbc:sqlite:app.db
```

## Tools Exposed to the LLM

| Tool | Purpose |
| --- | --- |
| `eval` | Execute tshell code. The LLM writes JS-syntax code with pipes; tshell runs it. |
| `help` | Runtime discovery. Searchable, shows examples and guides. |
| `prompt` | Returns compact language reference + all command signatures (~1460 tokens). Use this to populate the LLM's system prompt. |
