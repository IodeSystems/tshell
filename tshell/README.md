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

### Runtime Discovery

LLMs discover available capabilities at runtime via `help()` instead of needing them
encoded in the system prompt. When you add a toolkit, the LLM finds it automatically.

LLMs call help() to learn signatures and usage:

```javascript
help("sort") |> split("\n") |> limit(1) |> join("")
// → sort(input: array, key?: string)
```

Exact match shows full signature and docs:

```javascript
help("countBy") |> split("\n") |> limit(1) |> join("")
// → countBy(input: array, fn: (T) => string)
```

## Typical Usage

tshell reads like JavaScript and composes like a shell. These are the features you'll
use in most programs.

### Variables and Types

`let` declares variables. Types are inferred. All values are immutable data —
variables are reassignable but there is no mutation of values themselves.

```javascript
let x = 42; x
// → 42
```

```javascript
let name = "alice"; name
// → alice
```

```javascript
let active = true; active
// → true
```

```javascript
let nothing = null; nothing
// → null
```

```javascript
let items = [1, 2, 3]; items
// → [1, 2, 3]
```

```javascript
let obj = {name: "alice", age: 30}; obj.age
// → 30
```

Multi-binding — uninitialized vars are null:

```javascript
let a, b = 1, c = "two"; a
// → null
```

```javascript
let a, b = 1, c = "two"; c
// → two
```

### Functions

Named functions use `function`. Arrow functions work inline. Both capture their enclosing scope.

```javascript
function double(x) { return x * 2 }; double(21)
// → 42
```

```javascript
let sq = x => x * x; sq(7)
// → 49
```

```javascript
let add = (a, b) => a + b; add(3, 4)
// → 7
```

Template strings:

```javascript
function greet(who) { return `hello ${who}` }
greet("world")
// → hello world
```

### Pipes

The pipe operator `|>` passes the left-hand value as the first argument to the right-hand
function. This is the core composition mechanism — it replaces method chaining and lets
you build data pipelines. Use `|> fn` for zero-argument functions or `|> fn(args)` when
passing additional arguments.

```javascript
[3, 1, 2] |> sort
// → [1, 2, 3]
```

```javascript
[1, 2, 3] |> map(x => x * 10)
// → [10, 20, 30]
```

```javascript
[1, 2, 3, 4, 5] |> filter(x => x > 2)
// → [3, 4, 5]
```

```javascript
[1, 2, 3] |> reduce((sum, x) => sum + x, 0)
// → 6
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

### Control Flow

`if`/`else`, `while`, `for..of`, `break`, `continue`. Block-scoped — a `let` inside a
block does not leak out.

```javascript
let x = 10; if (x > 5) { "big" } else { "small" }
// → big
```

```javascript
let sum = 0
for (let x of [1, 2, 3]) { sum += x }
sum
// → 6
```

```javascript
let i = 0
while (i < 5) { i += 1 }
i
// → 5
```

Block scope — inner let doesn't leak:

```javascript
let x = 1
if (true) { let x = 99 }
x
// → 1
```

### String Operations

Built-in string commands for common transformations.

```javascript
"  hello  " |> trim()
// → hello
```

```javascript
"Hello" |> lower()
// → hello
```

```javascript
"hello" |> upper()
// → HELLO
```

```javascript
"hello world" |> replace("world", "tshell")
// → hello tshell
```

```javascript
"hello" |> startsWith("he")
// → true
```

```javascript
"hello" |> endsWith("lo")
// → true
```

```javascript
"hello world" |> indexOf("world")
// → 6
```

```javascript
"hello" |> substring(1, 4)
// → ell
```

```javascript
"abc123def456" |> match("[0-9]+")
// → ["123", "456"]
```

### Discovery

`help()` lists all available commands. `help("name")` shows detailed usage.
See [Runtime Discovery](#runtime-discovery) above for more.

help() returns a string listing all commands:

```javascript
typeof(help())
// → string
```

### Array Operations

Arrays support sort, reverse, unique, flat, limit, skip, find, and contains.

```javascript
[3, 1, 2] |> sort() |> reverse()
// → [3, 2, 1]
```

```javascript
[1, 2, 2, 3, 3] |> unique()
// → [1, 2, 3]
```

```javascript
[[1, 2], [3, 4]] |> flat()
// → [1, 2, 3, 4]
```

```javascript
[1, 2, 3, 4, 5] |> limit(3)
// → [1, 2, 3]
```

```javascript
[1, 2, 3, 4, 5] |> skip(2)
// → [3, 4, 5]
```

```javascript
[1, 2, 3] |> find(x => x > 1)
// → 2
```

```javascript
[1, 2, 3] |> contains(2)
// → true
```

```javascript
["b", "c", "a"] |> join(", ")
// → b, c, a
```

```javascript
range(1, 5)
// → [1, 2, 3, 4]
```

## Advanced Usage

Destructuring, null handling, composition, and the graph toolkit give you power
when simple pipes aren't enough.

### Reassignment and Compound Assignment

Variables declared with `let` can be reassigned. Assigning to a name that was never
declared is an error — this catches typos. Compound operators `+=`, `-=`, `*=` work
on variables and fields.

```javascript
let x = 1; x = 42; x
// → 42
```

```javascript
let x = 10; x -= 3; x
// → 7
```

```javascript
let x = 5; x *= 4; x
// → 20
```

### Index and Field Assignment

Assign to array indices and object fields, including nested paths.

```javascript
let arr = [1, 2, 3]; arr[1] = 99; arr
// → [1, 99, 3]
```

```javascript
let obj = {a: 1}; obj.b = 2; obj
// → {a: 1, b: 2}
```

```javascript
let obj = {a: {b: 1}}; obj.a.b = 42; obj.a.b
// → 42
```

Compound assignment on fields:

```javascript
let obj = {count: 0}; obj.count += 5; obj.count
// → 5
```

### Destructuring

Object and array destructuring work in `let` declarations and `for` loops.

```javascript
let {name, age} = {name: "alice", age: 30}; name
// → alice
```

```javascript
let [a, b, c] = [10, 20, 30]; b
// → 20
```

Rename during destructure:

```javascript
let {name: n} = {name: "bob"}; n
// → bob
```

### Spread Operator

Spread (`...`) works in arrays and objects to merge or extend values.

```javascript
let a = [1, 2]; [...a, 3, 4]
// → [1, 2, 3, 4]
```

```javascript
let a = {x: 1}; {...a, y: 2}
// → {x: 1, y: 2}
```

### Object Operations

Inspect and transform objects with keys, values, entries.

```javascript
{a: 1, b: 2} |> keys()
// → ["a", "b"]
```

```javascript
{a: 1, b: 2} |> values()
// → [1, 2]
```

```javascript
{a: 1, b: 2} |> entries()
// → [["a", 1], ["b", 2]]
```

### Math

Numeric operations for rounding, clamping, and power.

```javascript
3.7 |> floor()
// → 3
```

```javascript
3.2 |> ceil()
// → 4
```

```javascript
3.5 |> round()
// → 4
```

```javascript
-5 |> abs()
// → 5
```

```javascript
min(3, 7)
// → 3
```

```javascript
max(3, 7)
// → 7
```

```javascript
pow(2, 10)
// → 1024
```

### Type Conversion and Inspection

Convert between types and inspect values.

```javascript
str(42)
// → 42
```

```javascript
num("3.14")
// → 3.14
```

```javascript
typeof(42)
// → number
```

```javascript
typeof("hello")
// → string
```

```javascript
typeof([1, 2])
// → array
```

```javascript
typeof({a: 1})
// → object
```

```javascript
[1, 2, 3] |> len()
// → 3
```

### JSON

Parse and serialize JSON. Works with pipes.

```javascript
parseJson("[1, 2, 3]")
// → [1, 2, 3]
```

```javascript
parseJson("{\"name\": \"alice\"}")
// → {name: "alice"}
```

```javascript
{a: 1, b: [2, 3]} |> toJson()
// → {"a":1,"b":[2,3]}
```

### Ternary and Null Coalescing

Conditional expressions and null handling.

```javascript
true ? "yes" : "no" 
// → yes
```

```javascript
let x = null; x ?? "default" 
// → default
```

```javascript
let x = 42; x ?? "default" 
// → 42
```

### Optional Chaining

Safe property access on potentially null values.

```javascript
let x = {a: {b: 42}}; x?.a?.b
// → 42
```

```javascript
let x = null; x?.a?.b
// → null
```

### Composition

`chain` sequences functions. `all` runs functions and collects results.
`race` returns the first successful result.

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

### Scatter Pipe and toArray

The scatter pipe `|*` normalizes the left side to an array (`null` becomes `[]`,
a non-array becomes `[value]`), then runs the right-side function on each element
**in parallel**, returning an array of results. Use `toArray()` to normalize a
value to an array without mapping.

Scatter: parallel map over array:

```javascript
[1, 2, 3] |* (x => x * 10)
// → [10, 20, 30]
```

Scatter: null becomes empty array:

```javascript
null |* (x => x * 2)
// → []
```

Scatter: non-array wrapped then mapped:

```javascript
5 |* (x => x * 2)
// → [10]
```

Scatter then reduce:

```javascript
[1, 2, 3] |* (x => x * 2) |> reduce((sum, x) => sum + x)
// → 12
```

toArray: wraps non-array:

```javascript
5 |> toArray()
// → [5]
```

toArray: null becomes empty array:

```javascript
null |> toArray()
// → []
```

### Assertions

`assert` checks a condition and fails with a message if it's falsy.
Use it to enforce invariants on data, graph structure, or any intermediate result.

```javascript
assert("math works", 2 + 2 == 4)
// → null
```

```javascript
let items = [1, 2, 3]; assert("has items", items |> len() > 0)
// → null
```

### REPL Pattern

State persists across `eval` calls. Functions and variables from earlier evals
are available in later ones.

Define in one eval:

```javascript
let x = 42
// → 42
```

### Graph Toolkit

The graph toolkit provides a property graph with typed nodes, typed edges, and
pipe-based traversal. All nodes must connect to root. Traversal steps (`out`,
`inbound`, `both`) compose naturally with `filter`, `map`, `reduce`.

Create nodes and traverse:

```javascript
let alice = addNode(root(), "person", {name: "Alice", age: 30})
let bob = addNode(root(), "person", {name: "Bob", age: 25})
link(alice, bob, "knows")
root() |> out("person") |> map(n => n.name) |> sort()
// → ["Alice", "Bob"]
```

Typed edges for relationships:

```javascript
let alice = addNode(root(), "person", {name: "Alice"})
let acme = addNode(root(), "company", {name: "Acme"})
link(alice, acme, "worksAt")
let proj = addNode(acme, "project", {name: "TShell"})
link(alice, proj, "leads")
nodes("person") |> out("worksAt") |> map(n => n.name)
// → ["Acme"]
```

Reverse traversal with inbound:

```javascript
let a = addNode(root(), "person", {name: "A"})
let b = addNode(root(), "person", {name: "B"})
link(a, b, "friend")
nodes("person") |> filter(n => n.name == "B") |> inbound("friend") |> map(n => n.name)
// → ["A"]
```

Aggregate over traversal results:

```javascript
addNode(root(), "item", {value: 10})
addNode(root(), "item", {value: 20})
addNode(root(), "item", {value: 30})
root() |> out("item") |> map(n => n.value) |> reduce((sum, v) => sum + v, 0)
// → 60
```

### Error Handling

tshell provides clear error messages with suggestions. Unknown commands show
similar names. Type mismatches show what was expected. Execution limits prevent
runaway programs.

## Integration

tshell is text-in, text-out. Send source code, get results. State persists
across calls. Bind your own Kotlin functions as commands — the LLM discovers
them via `help()` without any prompt changes.

### Quick Start

```kotlin
val shell = TShell()
CoreToolkit.install(shell)

// Text in → text out
val result = shell.eval("[1, 2, 3] |> map(x => x * 2)")
println(result.toDisplayString()) // "[2, 4, 6]"

// State persists across evals
shell.eval("let x = 42")
shell.eval("x")  // → 42

// Transactional eval — rolls back on failure
shell.evalTransactional("x = 99; fail(\"boom\")")
shell.eval("x")  // → 42 (unchanged)
```

### Bind Your Own Commands

Register any Kotlin function as a tshell command. It appears in `help()` with
its signature, description, and examples — the LLM discovers it automatically.

```kotlin
shell.register(
  "greet", "name: string",
  "returns a greeting for the given name",
  listOf("greet(\"world\")")
) { args ->
  val name = (args[0] as TShellValue.TString).value
  TShellValue.TString("hello $name")
}

// Now in tshell: greet("world") // → "hello world"
// And: help("greet") shows the signature and examples
```

This is the core integration pattern: your domain logic lives in Kotlin,
the LLM composes it via tshell. Add a database query command, an API call,
a domain calculation — the LLM chains them with pipes, loops, and data
transforms without needing separate tool schemas for each one.

### LLM Agent Integration (Koog)

Wrap tshell as a single tool for any LLM agent framework. Here's the
complete binding for [Koog](https://github.com/JetBrains/koog):

```kotlin
@LLMDescription("tshell scripting")
class TShellTools(private val shell: TShell) : ToolSet {
  @Tool
  @LLMDescription(TShell.TOOL_DESCRIPTION)
  fun tshell(@LLMDescription("tshell source code") code: String): String {
    return try {
      shell.evalExported(code).toDisplayString()
    } catch (e: Exception) {
      "ERROR: ${e.message}"
    }
  }
}

// Create the agent
val shell = TShell()
CoreToolkit.install(shell)
FileToolkit(Path.of("/workspace")).install(shell)

val agent = AIAgent(
  model = OpenAIModels.chatGPT4o,
  systemPrompt = "You have a tshell tool for computation.\n" + shell.toPrompt(),
  tools = listOf(TShellTools(shell))
)
```

That's it — one class, one tool. The LLM gets computation, file I/O, data
processing, and any custom commands you register. `shell.toPrompt()` generates
the syntax reference and command signatures automatically.

The same pattern works for building MCP servers, OpenAI function-calling
integrations, or any framework that supports tool use. The binding is always
the same: text in, text out.

See [`tshell-sample-koog`](tshell-sample-koog/) for a complete working example
with a local LLM, including benchmarks.

### Execution Limits

tshell sandboxes execution to prevent runaway programs.

```kotlin
val shell = TShell(
  maxSteps = 1_000_000,   // max AST node evaluations (default)
  maxCallDepth = 256,     // max recursion depth (default)
  timeoutMs = 30_000      // wall-clock timeout in ms (default)
)
```

From within tshell, `limits()` shows current limits and `extendLimit({steps: n, timeout: ms, callDepth: n, outputBytes: n})`
can increase them when the LLM needs heavier computation.

### State Serialization

Save and restore session state for multi-turn interactions.

```kotlin
val state = shell.getState()

val newShell = TShell()
CoreToolkit.install(newShell)
newShell.setState(state)

// Or inject specific bindings
newShell.setState(mapOf("user" to TShellValue.TString("alice")))
```

## Modules

tshell is modular. Install only what you need — each module adds commands
that the LLM discovers via `help()`. All modules are published to Maven Central
under `com.iodesystems.tshell`.

### Core (`tshell`)

```kotlin
implementation("com.iodesystems.tshell:tshell:0.1.1")
```

The language runtime plus `CoreToolkit`: pipes, arrays, strings, math, JSON,
composition (`all`, `race`, `chain`), and `help()`. Also includes:

| Toolkit | Install | Commands |
| --- | --- | --- |
| **CoreToolkit** | `CoreToolkit.install(shell)` | `map`, `filter`, `reduce`, `sort`, `join`, `split`, `keys`, `values`, `entries`, `groupBy`, `countBy`, `range`, `parseJson`, `toJson`, and [more](#typical-usage) |
| **MathToolkit** | `MathToolkit().install(shell)` | `Math.sin`, `Math.cos`, `Math.sqrt`, `Math.log`, `Math.PI`, `Math.E`, etc. |
| **WebToolkit** | `WebToolkit().install(shell)` | `Web.fetch`, `Web.fetchText`, `Web.search`, `Html.select`, `Html.links`, `Html.text`, `Html.table` |
| **FileToolkit** | `FileToolkit(rootPath).install(shell)` | Sandboxed `read`, `write`, `glob`, `grep`, `edit` — LLM can only access files under `rootPath` |
| **GraphToolkit** | `GraphToolkit(store).install(shell)` | Property graph: `addNode`, `link`, `out`, `inbound`, `nodes`, `setProps` — see [Graph Toolkit](#graph-toolkit) |

### Browser Automation (`tshell-playwright`)

```kotlin
implementation("com.iodesystems.tshell:tshell-playwright:0.1.1")
```

Playwright-based browser automation. The LLM can navigate pages, click elements,
fill forms, read text, and take screenshots — all through tshell commands.

```kotlin
val browser = BrowserToolkit(headless = true)
browser.install(shell)
// Commands: Browser.open, Browser.click, Browser.type, Browser.text,
//           Browser.select, Browser.screenshot, Browser.eval, ...
```

### SQL (`tshell-sql`)

```kotlin
implementation("com.iodesystems.tshell:tshell-sql:0.1.1")
```

JDBC-based SQL toolkit. Supports multiple databases, read-only mode by default,
parameterized queries, and schema introspection.

```kotlin
val sql = SqlToolkit(mapOf("db" to DatabaseConfig(dataSource)))
sql.install(shell)
// Commands: db.query, db.tables, db.columns, db.schema
// Read-only by default — LLM must call db.requestWrite() for mutations
```

### Sample Agent (`tshell-sample-koog`)

Complete working example: a CLI chat agent powered by a local LLM (any
OpenAI-compatible API) with tshell as its computation tool. Includes
benchmarks proving LLMs can solve problems via code that they can't solve
by reasoning alone (96% pass rate on 32 coding challenges).

See [`tshell-sample-koog/README.md`](tshell-sample-koog/README.md) for setup.

## Extending tshell

### Toolkit Extensions (.tshell files)

Package reusable functions as `.tshell` files in a directory structure:

```
.tsh/
  math/
    stats.tshell           # function mean(arr) { ... }
    typical_usage.md       # shown in help("math")
  text/
    nlp.tshell
    typical_usage.md
```

```kotlin
shell.loadToolkits(Path.of(".tsh"))

// Or namespace to avoid collisions
shell.loadToolkits(Path.of(".tsh"), mapOf("math" to "m"))
// m.mean([1, 2, 3]) instead of mean([1, 2, 3])
```

### Graph Schema Enforcement

Define a schema to constrain what the LLM can add to the graph.

```kotlin
val schema = GraphSchema(
  nodes = mapOf(
    "person" to NodeSchema(required = setOf("name"), optional = setOf("age", "email")),
    "company" to NodeSchema(required = setOf("name")),
  ),
  edges = mapOf(
    "worksAt" to EdgeSchema(from = "person", to = "company"),
    "knows" to EdgeSchema(from = "person", to = "person"),
  ),
  strict = true // reject unknown node/edge types
)
GraphToolkit(InMemoryGraphStore(), schema = schema).install(shell)
```

The schema is enforced eagerly and included in `help("graph")` so the
LLM knows what's valid.

---

*This README is generated from
[`TShellLiterateTest.kt`](src/test/kotlin/com/iodesystems/tshell/TShellLiterateTest.kt).
Every code example above is executed as part of the test run — if an example
is wrong, the build fails.*
