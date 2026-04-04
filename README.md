# tshell

One `eval` tool instead of twenty. Sandboxed JS syntax your LLM already knows.

- **LLMs compute instead of reasoning** — "how many R's in strawberry?" becomes `.split("").filter(c => c == "r").length`
- **~95% less tool context** — one `eval` tool replaces dozens of MCP tool schemas; the LLM prompt stays constant as you add capabilities
- **Polyglot composition** — chain tools from Python, Go, TypeScript MCP servers with pipes in a single `eval` call
- **KV cache friendly** — system prompt doesn't change when tools change; `help()` discovers capabilities at runtime
- **No double-encoding corruption** — raw template strings (`r\`...\``) let LLMs write backslashes, quotes, and code exactly as-is. No more `\\\\` spirals when editing files with Windows paths or regex patterns

**Total stdlib context cost: 6.2KB / 1758 tokens** (full) · **2.7KB / 757 tokens** (compact) — cl100k_base

## Quick Start

Build and run as an MCP server:

```bash
git clone https://github.com/IodeSystems/tshell.git && cd tshell
./gradlew :tshell-cli:installDist
```

Add to your MCP client (Claude Desktop, Cursor, etc.):

```json
{
  "mcpServers": {
    "tshell": {
      "command": "./tshell-cli/build/install/tshell-cli/bin/tshell-cli"
    }
  }
}
```

Your LLM now has `eval` and `help`. It writes JS-syntax code; tshell executes it.

```javascript
// LLM can compute instead of reasoning:
"strawberry".split("").filter(c => c == "r").length  // → 3

// Chain operations in a single tool call:
let data = [{name: "Alice", score: 85}, {name: "Bob", score: 92}]
data |> filter(d => d.score > 80) |> map(d => d.name) |> join(", ")  // → "Alice, Bob"
```

## MCP Composition

Connect external MCP servers — their tools become tshell commands, composed with pipes.
Use `--mcp` with the standard JSON config format, or `--connect` for shorthand:

```bash
# Standard MCP JSON config — same format as Claude Desktop, Cursor, etc.
tshell-cli --mcp '{"mcpServers": {"browser": {"command": "npx", "args": ["@anthropic/mcp-playwright"]}}}'

# Or point to a config file
tshell-cli --mcp mcp-config.json

# Shorthand for simple cases
tshell-cli --connect "app=python my_tools.py" --connect "data=npx data-server"
```

```
LLM
 │
 ▼
tshell-cli (single MCP server)
 ├── eval("app.users() |> filter(u => u.active) |> sort(\"name\")")
 │    ├── tshell core (pipes, filter, sort, strings, math)
 │    ├── app.* → Python MCP server (your app code)
 │    └── data.* → Go MCP server (your data pipeline)
 └── help() → all commands from all servers
```

**Context reduction:** each MCP tool inflates the LLM's prompt with schema and
descriptions. A Playwright MCP server adds ~8KB of tool definitions. tshell
replaces all of it with one `eval` tool — commands are discovered via `help()`
at runtime, not baked into the prompt. The KV cache survives tool set changes.

See [`tshell-cli/README.md`](tshell-cli/README.md) for CLI flags, Claude Desktop config, and JDBC driver setup.

## Why tshell

LLMs are bad at computation. They can't reliably count letters, do arithmetic on large
numbers, or sort data by reasoning alone. But they're excellent at writing code —
especially JavaScript, which dominates their training data.

tshell gives your LLM a single `eval` tool with JS syntax. Instead of defining dozens of
tools (each requiring schema, validation, and prompt engineering), you get one tool that
covers computation, string processing, data transformation, and more. Capabilities are
discovered at runtime via `help()`, so the system prompt stays small and the KV cache
survives tool set changes.

### Computation Beats Reasoning

LLMs famously fail at tasks like "how many R's in strawberry?" because they try to
reason about it instead of computing it. With tshell, they write code instead.

Count letters — LLMs get this wrong by reasoning, right by computing:

```javascript
"strawberry".split("").filter(c => c == "r").length
// → 3
```

Word frequency — multi-step analysis in one expression:

```javascript
let words = "the quick brown fox jumps over the lazy dog the fox"
let counts = words |> split(" ") |> countBy(w => w)
entries(counts) |> filter(e => e[1] > 1) |> map(e => `${e[0]}=${e[1]}`) |> join(", ")
// → the=3, fox=2
```

### One Tool Replaces Many

Without tshell, an LLM needs separate tools for search, math, string processing, data
transformation — each requiring a round-trip. With tshell, complex multi-step operations
happen in a single `eval` call.

Filter, aggregate, and format — three tool calls collapsed into one:

```javascript
let data = [{name: "Alice", score: 85}, {name: "Bob", score: 92}, {name: "Carol", score: 78}]
let avg = data |> map(d => d.score) |> reduce((sum, s) => sum + s, 0) |> (total => total / (data |> len()))
let above = data |> filter(d => d.score >= avg)
above |> map(d => `${d.name}: ${d.score}`) |> join(", ")
// → Alice: 85, Bob: 92
```

Parse and restructure — no special date-parsing tool needed:

```javascript
"2024-03-14T10:30:00" |> split("T") |> (parts => {
  let [date, time] = parts
  let [y, m, d] = date |> split("-")
  {year: num(y), month: num(m), day: num(d), time: time}
})
// → {year: 2024, month: 3, day: 14, time: "10:30:00"}
```

## What's Different from JS

tshell is a JS subset — `let`, `function`, `if`/`else`, `for`/`while`, arrow functions,
destructuring, spread, template strings, `?.`, `??`, ternary all work as expected.
These are the additions and differences.

### Pipes

The pipe operator `|>` passes the left-hand value as the first argument to the right-hand
function. This replaces method chaining and is the primary way to compose operations.

```javascript
[3, 1, 2] |> sort
// → [1, 2, 3]
```

```javascript
[1, 2, 3] |> map(x => x * 10)
// → [10, 20, 30]
```

Chained pipes:

```javascript
"hello world" |> split(" ") |> map(w => w |> upper()) |> join(" ")
// → HELLO WORLD
```

Pipe into arrow function:

```javascript
5 |> (x => x * x)
// → 25
```

### Scatter Pipe

`|*` normalizes the left side to an array, then maps the right-side function over each
element **in parallel**.

```javascript
[1, 2, 3] |* (x => x * 10)
// → [10, 20, 30]
```

null becomes empty array:

```javascript
null |* (x => x * 2)
// → []
```

non-array wrapped then mapped:

```javascript
5 |* (x => x * 2)
// → [10]
```

### Commands

Standard operations are pipe-friendly commands instead of methods. The LLM discovers
them via `help()`. Common commands:

```javascript
[1, 2, 3] |> filter(x => x > 1) |> reduce((sum, x) => sum + x, 0)
// → 5
```

Comparator sort:

```javascript
[3, 1, 2] |> sort((a, b) => b - a)
// → [3, 2, 1]
```

Descending sort:

```javascript
[3, 1, 2] |> sort("desc")
// → [3, 2, 1]
```

```javascript
{a: 1, b: 2} |> entries()
// → [["a", 1], ["b", 2]]
```

```javascript
range(1, 5)
// → [1, 2, 3, 4]
```

help() returns all available commands:

```javascript
typeof(help())
// → string
```

### Export and State

State is discarded after each `eval` call unless explicitly exported. Use `export` to
persist values across tool calls in multi-turn LLM conversations.

Without export, x is gone after this eval:

```javascript
let x = 42
// → 42
```

### Composition

`chain` sequences functions. `all` runs in parallel and collects results.
`race` returns the first success.

```javascript
chain(() => [1, 2, 3], arr => arr |> map(x => x * 2), arr => arr |> join(", "))
// → 2, 4, 6
```

```javascript
all(() => 1 + 2, () => 3 * 4)
// → [3, 12]
```

```javascript
race(() => fail("down"), () => "fallback")
// → fallback
```

### Raw Template Strings

What about double-encoding corruption? LLM writes `\\n`, transport eats the backslash,
interpreter gets a newline. Or the LLM spirals through `\\\\`, `\\\\\\\\`, never landing on the
right number of backslashes. Raw template strings fix this: prefix any template literal
with `r` to disable backslash escape processing. What you type is what you get.
`${expr}` interpolation still works — Kotlin-style: raw content, live substitutions.

Backslashes are literal:

```javascript
r`C:\Users\foo`
// → C:\Users\foo
```

No escape processing — \n stays as two characters:

```javascript
r`line1\nline2`
// → line1\nline2
```

Interpolation still works:

```javascript
let name = "world"; r`Hello ${name}, path=C:\Users`
// → Hello world, path=C:\Users
```

Regular template for comparison — \t becomes a tab:

```javascript
`tab\there`
// → tab	here
```

Multiline template strings:

```javascript
`line1
line2
line3`
// → line1
line2
line3
```

Raw interpolation with literal backslash — \n stays as two characters:

```javascript
let x = 42; r`result: ${x}\nend`
// → result: 42\nend
```

### What's Missing

No `class`, `this`, `new`, `async`/`await`, `import`/`require`, prototypes,
generators, or `Symbol`. Type annotations are accepted but ignored.
Use `help()` to discover what's available.

## Programmatic Integration

Published to Maven Central as `com.iodesystems.tshell:tshell`.

```kotlin
// build.gradle.kts
dependencies {
  implementation("com.iodesystems.tshell:tshell:0.1.0")
}
```

```kotlin
val shell = TShell()
CoreToolkit.install(shell)

val result = shell.eval("[1, 2, 3] |> map(x => x * 2)")
println(result.toDisplayString()) // "[2, 4, 6]"
```

### Custom Commands

Register Kotlin functions as tshell commands. They appear in `help()`
automatically — the LLM discovers them without prompt changes.

```kotlin
shell.register(
  "greet", "name: string",
  "returns a greeting for the given name",
  listOf("greet(\"world\")")
) { args ->
  val name = (args[0] as TShellValue.TString).value
  TShellValue.TString("hello $name")
}
```

### Execution Limits

```kotlin
val shell = TShell(
  maxSteps = 1_000_000,   // max AST node evaluations
  maxCallDepth = 256,     // max recursion depth
  timeoutMs = 30_000      // wall-clock timeout
)
```

## Modules

| Module | Artifact | Provides |
| --- | --- | --- |
| **Core** | `tshell` | Language runtime, `CoreToolkit` (pipes, arrays, strings, math, JSON, composition), `MathToolkit`, `WebToolkit`, `FileToolkit` |
| **Graph** | `tshell-graph` | Graph database toolkit: nodes, edges, traversal, schema validation. See [`tshell-graph/README.md`](tshell-graph/README.md) |
| **MCP** | `tshell-mcp` | MCP server + client library. See [`tshell-mcp/README.md`](tshell-mcp/README.md) |
| **CLI** | `tshell-cli` | Standalone MCP server binary with toolkits, external server composition, JDBC drivers. See [`tshell-cli/README.md`](tshell-cli/README.md) |
| **Browser** | `tshell-playwright` | Lean Playwright automation (12 commands, ~800 chars context vs ~8KB for `@playwright/mcp`). See [`tshell-playwright/README.md`](tshell-playwright/README.md) |
| **SQL** | `tshell-sql` | JDBC toolkit: `db.query`, `db.tables`, `db.schema`. Read-only by default |
| **Sample** | `tshell-sample-koog` | CLI chat agent + benchmarks (100% on 32 challenges). See [`tshell-sample-koog/README.md`](tshell-sample-koog/README.md) |

## Context Budget

How much prompt space does tshell cost? Measured with cl100k_base (GPT-4 tokenizer),
computed at build time from actual prompt content.

| Component | Full | Compact | What it contains |
| --- | ---: | ---: | --- |
| Tool description | 112 | 112 | One-line summary in the tool schema |
| Syntax reference | 435 | 435 | JS subset, pipes, raw templates, composition |
| Command signatures | 1183 | 182 | All stdlib commands (compact: names only, use `help()`) |
| **Total** | **1758** | **757** | |

Compact mode lists command names only — the LLM calls `help("name")` for signatures and `help("namespace")` for namespace overviews.
For comparison: a typical MCP tool server exposes 10-20 tools at ~200 tokens each (2000-4000 tokens).
tshell replaces them all with one `eval` tool. Additional commands added via toolkits
(FileToolkit, SqlToolkit, etc.) grow only the command signatures section.

See [benchmark results](benchmarks/results/README.md) for how this prompt performs across 33 challenges.

---

*This README is generated from
[`TShellLiterateTest.kt`](tshell/src/test/kotlin/com/iodesystems/tshell/TShellLiterateTest.kt).
Every code example above is executed as part of the test — if an example
is wrong, the build fails.*
