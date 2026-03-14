# tshell

One tool instead of twenty. Give your LLM `eval` and let it compute.

tshell is a sandboxed scripting language with JavaScript syntax — the one
language every LLM already knows. Instead of building separate tools for
search, math, string processing, and data transformation (each needing
schema, validation, and prompt engineering), you give your LLM a single
`eval` tool that handles all of it. Capabilities are discovered at runtime
via `help()`, so the system prompt stays small and the KV cache survives
tool set changes.

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
"strawberry" |> split("") |> filter(c => c == "r") |> len()
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

### What's Missing

No `class`, `this`, `new`, `var`, `const`, `async`/`await`, `try`/`catch`,
`import`/`require`, prototypes, generators, or `Symbol`. Type annotations are
accepted but ignored. Use `help()` to discover what's available.

## Integration

### Quick Start

```kotlin
val shell = TShell()
CoreToolkit.install(shell)

val result = shell.eval("[1, 2, 3] |> map(x => x * 2)")
println(result.toDisplayString()) // "[2, 4, 6]"
```

### Bind Your Own Commands

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

This is the core pattern: domain logic in Kotlin, composed via tshell.
Add a database query, an API call, a domain calculation — the LLM chains
them with pipes without needing separate tool schemas.

### LLM Agent Integration (Koog)

Wrap tshell as a single tool for any agent framework:

```kotlin
@LLMDescription("tshell scripting")
class TShellTools(private val shell: TShell) : ToolSet {
  @Tool @LLMDescription(TShell.TOOL_DESCRIPTION)
  fun tshell(@LLMDescription("tshell source code") code: String): String =
    try { shell.evalExported(code).toDisplayString() }
    catch (e: Exception) { "ERROR: ${e.message}" }
}
```

`shell.toPrompt()` generates the syntax reference and command signatures
for the system prompt. Same pattern works for MCP servers or OpenAI function calling.

See [`tshell-sample-koog`](tshell-sample-koog/) for a complete working example
with benchmarks (96% on 32 coding challenges with a local LLM).

### Execution Limits

```kotlin
val shell = TShell(
  maxSteps = 1_000_000,   // max AST node evaluations
  maxCallDepth = 256,     // max recursion depth
  timeoutMs = 30_000      // wall-clock timeout
)
```

The LLM can call `extendLimit({steps: n})` when it needs heavier computation.

## Modules

All published to Maven Central under `com.iodesystems.tshell`.

| Module | Artifact | Provides |
| --- | --- | --- |
| **Core** | `tshell` | Language runtime, `CoreToolkit` (pipes, arrays, strings, math, JSON, composition), `MathToolkit`, `WebToolkit`, `FileToolkit`, `GraphToolkit`. See [Graph docs](README-graph.md) |
| **Browser** | `tshell-playwright` | Playwright-based automation: `Browser.open`, `.click`, `.type`, `.text`, `.screenshot` |
| **SQL** | `tshell-sql` | JDBC toolkit: `db.query`, `db.tables`, `db.schema`. Read-only by default |
| **Sample Agent** | `tshell-sample-koog` | CLI chat agent with local LLM + benchmarks. See [`tshell-sample-koog/README.md`](tshell-sample-koog/README.md) |

---

*This README is generated from
[`TShellLiterateTest.kt`](src/test/kotlin/com/iodesystems/tshell/TShellLiterateTest.kt).
Every code example above is executed as part of the test — if an example
is wrong, the build fails.*
