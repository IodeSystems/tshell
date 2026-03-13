# tshell

A sandboxed scripting language that gives LLMs safe, expressive computation
through a single `eval` tool.

Instead of defining dozens of individual tools — each requiring schema,
validation, and prompt engineering — give your LLM one tool: `eval`. tshell's
TypeScript-like syntax means LLMs can write it without new training.
Capabilities are discovered at runtime via `help()`, and the KV cache is
preserved across tool set changes.

## Typical Usage

tshell reads like TypeScript and composes like a shell. These are the features you'll
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

The pipe operator `|` passes the left-hand value as the first argument to the right-hand
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

`help()` lists all available commands. `help("search")` filters by keyword.
This is how LLMs discover capabilities at runtime without needing them baked
into the system prompt.

help() returns a string listing all commands:

```javascript
typeof(help())
// → string
```

help(name) shows detailed usage:

```javascript
help("sort") |> split("\n") |> limit(1) |> join("")
// → sort(input: array, key?: string)
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
// → [{key: "a", value: 1}, {key: "b", value: 2}]
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
// → null
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

tshell is designed for text-in, text-out integration. Your host application
sends tshell source code as a string and receives results as a string. State
persists across calls, enabling multi-step interactions.

### Kotlin / JVM

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

### Adding Capabilities

```kotlin
// Graph toolkit
GraphToolkit(InMemoryGraphStore()).install(shell)

// Load toolkit extensions from a directory
shell.loadToolkits(Path.of(".tsh"))

// Namespaced loading
shell.loadToolkits(Path.of(".tsh"), mapOf("graph" to "g"))
// Now: g.addNode(...), g.out(...)
```

### LLM System Prompt Generation

```kotlin
// Generate a system prompt describing all available commands
val prompt = shell.toPrompt()
```

The generated prompt includes syntax, available commands, and their
signatures — everything an LLM needs to write tshell code.

### Execution Limits

tshell sandboxes execution to prevent runaway programs.

```kotlin
val shell = TShell(
  maxSteps = 1_000_000,   // max execution steps (default)
  maxCallDepth = 256,     // max recursion depth (default)
  timeoutMs = 30_000      // wall-clock timeout in ms (default)
)
```

From within tshell, `limits()` shows current limits and `extendLimit({steps: n, timeout: ms, callDepth: n, outputBytes: n})`
can increase them.

### State Serialization

Save and restore session state for multi-turn interactions.

```kotlin
// Capture all global bindings
val state = shell.getState()

// Restore into a new shell instance
val newShell = TShell()
CoreToolkit.install(newShell)
newShell.setState(state)

// Or inject specific bindings
newShell.setState(mapOf("user" to TShellValue.TString("alice")))
```

## Extending tshell

Register custom commands from your host language, or package reusable
functions as toolkit extensions.

### Custom Commands (Kotlin)

```kotlin
shell.register(
  "greet", "name: string",
  "returns a greeting for the given name",
  listOf("greet(\"world\")")
) { args ->
  val name = (args[0] as TShellValue.TString).value
  TShellValue.TString("hello $name")
}

// Now available in tshell: greet("world") // → "hello world"
```

Commands registered this way appear in `help()` with their signature,
description, and examples.

### Toolkit Extensions (.tshell files)

Package reusable functions as `.tshell` files in a directory structure:

```
.tsh/
  math/
    stats.tshell           # function mean(arr) { ... }
    typical_usage.md       # shown in help("math")
    advanced_usage.md
  text/
    nlp.tshell
    typical_usage.md
```

```kotlin
// Load all toolkits — functions become global
shell.loadToolkits(Path.of(".tsh"))

// Or namespace them to avoid collisions
shell.loadToolkits(Path.of(".tsh"), mapOf("math" to "m"))
// m.mean([1, 2, 3]) instead of mean([1, 2, 3])
```

Toolkit files are evaluated in alphabetical order. Prefix with numbers
(`01_base.tshell`, `02_derived.tshell`) to control load order when files
depend on each other.

## Optional Modules

tshell ships with optional modules that can be installed independently.
Only `CoreToolkit` is required.

| Module |> Install |> Provides |
| --- |> --- |> --- |
| **CoreToolkit** |> `CoreToolkit.install(shell)` |> Pipes, arrays, strings, math, JSON, composition |
| **GraphToolkit** |> `GraphToolkit(store).install(shell)` |> Property graph with typed nodes/edges and traversal |
| **FileToolkit** |> `FileToolkit(rootPath).install(shell)` |> Sandboxed file I/O: read, write, glob, grep, edit |

```kotlin
// Minimal setup — just the language + core functions
val shell = TShell()
CoreToolkit.install(shell)

// Add file operations (sandboxed to a root directory)
FileToolkit(Path.of("/workspace")).install(shell)

// Add graph operations
GraphToolkit(InMemoryGraphStore()).install(shell)
```

### Graph Schema Enforcement

Define a schema to prevent LLMs from adding invalid data to the graph.
Schemas are enforced eagerly — `addNode`, `link`, and `setProps` fail
immediately if they violate constraints. The LLM cannot modify the schema.

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
  strict = true // reject unknown node/edge types entirely
)

GraphToolkit(InMemoryGraphStore(), schema = schema).install(shell)
```

With this schema in place:

```javascript
addNode(root(), "person", {age: 30})        // ERROR: missing required 'name'
addNode(root(), "alien", {name: "X"})       // ERROR: unknown node type (strict)
link(company, person, "worksAt")             // ERROR: worksAt must originate from 'person'
```

The schema is automatically included in `help("graph")` so LLMs know
what types and properties are valid.

---

*This README is generated from
[`TShellLiterateTest.kt`](src/test/kotlin/com/iodesystems/tshell/TShellLiterateTest.kt).
Every code example above is executed as part of the test run — if an example
is wrong, the build fails.*
