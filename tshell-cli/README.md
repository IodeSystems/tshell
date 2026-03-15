# tshell-cli

Standalone MCP server binary. Wraps tshell core + optional toolkits behind a single
`eval` tool for LLMs.

## Build

```bash
./gradlew :tshell-cli:installDist
```

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

## CLI Flags

| Flag | Purpose |
| --- | --- |
| `--files-dir PATH` | Enable file toolkit rooted at PATH |
| `--files-read-only` | File toolkit is read-only (default: writable) |
| `--sql-url URL` | JDBC URL (repeatable, `name=jdbc:...` for named). Default: read-only |
| `--sql-writable` | Allow SQL writes |
| `--web` | Enable Web.fetch, Web.search, Html.* |
| `--mcp SPEC` | MCP server: file path, inline JSON, or `name:command args...` |
| `--connect SPEC` | Shorthand: `command args...` or `namespace=command args...` |
| `--timeout MS` | Execution timeout (default: 30000) |
| `--max-steps N` | Max execution steps (default: 1000000) |
| `--max-output N` | Max output bytes (default: 16000) |

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
