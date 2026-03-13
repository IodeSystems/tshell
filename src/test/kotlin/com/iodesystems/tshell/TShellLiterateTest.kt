package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import com.iodesystems.tshell.toolkit.graph.GraphToolkit
import com.iodesystems.tshell.toolkit.graph.InMemoryGraphStore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Literate test suite for tshell.
 *
 * Each [Example] is a tshell snippet with an expected output. The [generateReadme]
 * test assembles them into a README.md that is always in sync with actual behavior.
 *
 * If any snippet fails to produce its expected output, the test fails — so the
 * README can never contain a broken example.
 */
class TShellLiterateTest {

  data class Example(
    val code: String,
    val expect: String,
    val description: String = ""
  )

  data class Section(
    val title: String,
    val prose: String,
    val examples: List<Example>
  )

  data class SectionGroup(
    val title: String,
    val prose: String = "",
    val sections: List<Section>
  )

  // --- Shared shells ---

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  private fun graphShell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    GraphToolkit(InMemoryGraphStore()).install(sh)
    return sh
  }

  // --- The literate spec ---

  private val groups = listOf(

    // ======== TYPICAL USAGE ========

    SectionGroup("Typical Usage", """
tshell reads like TypeScript and composes like a shell. These are the features you'll
use in most programs.
    """.trim(), listOf(

      Section("Variables and Types", """
`let` declares variables. Types are inferred. All values are immutable data —
variables are reassignable but there is no mutation of values themselves.
      """.trim(), listOf(
        Example("""let x = 42; x""", "42"),
        Example("""let name = "alice"; name""", "alice"),
        Example("""let active = true; active""", "true"),
        Example("""let nothing = null; nothing""", "null"),
        Example("""let items = [1, 2, 3]; items""", "[1, 2, 3]"),
        Example("""let obj = {name: "alice", age: 30}; obj.age""", "30"),
      )),

      Section("Functions", """
Named functions use `function`. Arrow functions work inline. Both capture their enclosing scope.
      """.trim(), listOf(
        Example("""function double(x) { return x * 2 }; double(21)""", "42"),
        Example("""let sq = x => x * x; sq(7)""", "49"),
        Example("""let add = (a, b) => a + b; add(3, 4)""", "7"),
        Example("""
function greet(who) { return `hello ${'$'}{who}` }
greet("world")
        """.trim(), "hello world", "Template strings"),
      )),

      Section("Pipes", """
The pipe operator `|` passes the left-hand value as the first argument to the right-hand
function. This is the core composition mechanism — it replaces method chaining and lets
you build data pipelines. Use `|> fn` for zero-argument functions or `|> fn(args)` when
passing additional arguments.
      """.trim(), listOf(
        Example("""[3, 1, 2] |> sort""", "[1, 2, 3]"),
        Example("""[1, 2, 3] |> map(x => x * 10)""", "[10, 20, 30]"),
        Example("""[1, 2, 3, 4, 5] |> filter(x => x > 2)""", "[3, 4, 5]"),
        Example("""[1, 2, 3] |> reduce((sum, x) => sum + x, 0)""", "6"),
        Example(""""hello world" |> split(" ") |> map(w => w |> upper()) |> join(" ")""",
          "HELLO WORLD", "Chained pipes"),
        Example("""5 |> (x => x * x)""", "25", "Pipe into arrow function"),
      )),

      Section("Control Flow", """
`if`/`else`, `while`, `for..of`, `break`, `continue`. Block-scoped — a `let` inside a
block does not leak out.
      """.trim(), listOf(
        Example("""let x = 10; if (x > 5) { "big" } else { "small" }""", "big"),
        Example("""
let sum = 0
for (let x of [1, 2, 3]) { sum += x }
sum
        """.trim(), "6"),
        Example("""
let i = 0
while (i < 5) { i += 1 }
i
        """.trim(), "5"),
        Example("""
let x = 1
if (true) { let x = 99 }
x
        """.trim(), "1", "Block scope — inner let doesn't leak"),
      )),

      Section("String Operations", """
Built-in string commands for common transformations.
      """.trim(), listOf(
        Example(""""  hello  " |> trim()""", "hello"),
        Example(""""Hello" |> lower()""", "hello"),
        Example(""""hello" |> upper()""", "HELLO"),
        Example(""""hello world" |> replace("world", "tshell")""", "hello tshell"),
        Example(""""hello" |> startsWith("he")""", "true"),
        Example(""""hello" |> endsWith("lo")""", "true"),
        Example(""""hello world" |> indexOf("world")""", "6"),
        Example(""""hello" |> substring(1, 4)""", "ell"),
        Example(""""abc123def456" |> match("[0-9]+")""", """["123", "456"]"""),
      )),

      Section("Discovery", """
`help()` lists all available commands. `help("search")` filters by keyword.
This is how LLMs discover capabilities at runtime without needing them baked
into the system prompt.
      """.trim(), listOf(
        Example("""typeof(help())""", "string", "help() returns a string listing all commands"),
        Example("""help("sort") |> split("\n") |> limit(1) |> join("")""",
          "sort(input: array, key?: string)", "help(name) shows detailed usage"),
      )),

      Section("Array Operations", """
Arrays support sort, reverse, unique, flat, limit, skip, find, and contains.
      """.trim(), listOf(
        Example("""[3, 1, 2] |> sort() |> reverse()""", "[3, 2, 1]"),
        Example("""[1, 2, 2, 3, 3] |> unique()""", "[1, 2, 3]"),
        Example("""[[1, 2], [3, 4]] |> flat()""", "[1, 2, 3, 4]"),
        Example("""[1, 2, 3, 4, 5] |> limit(3)""", "[1, 2, 3]"),
        Example("""[1, 2, 3, 4, 5] |> skip(2)""", "[3, 4, 5]"),
        Example("""[1, 2, 3] |> find(x => x > 1)""", "2"),
        Example("""[1, 2, 3] |> contains(2)""", "true"),
        Example("""["b", "c", "a"] |> join(", ")""", "b, c, a"),
        Example("""range(1, 5)""", "[1, 2, 3, 4]"),
      )),

    )),

    // ======== ADVANCED USAGE ========

    SectionGroup("Advanced Usage", """
Destructuring, null handling, composition, and the graph toolkit give you power
when simple pipes aren't enough.
    """.trim(), listOf(

      Section("Reassignment and Compound Assignment", """
Variables declared with `let` can be reassigned. Assigning to a name that was never
declared is an error — this catches typos. Compound operators `+=`, `-=`, `*=` work
on variables and fields.
      """.trim(), listOf(
        Example("""let x = 1; x = 42; x""", "42"),
        Example("""let x = 10; x -= 3; x""", "7"),
        Example("""let x = 5; x *= 4; x""", "20"),
      )),

      Section("Index and Field Assignment", """
Assign to array indices and object fields, including nested paths.
      """.trim(), listOf(
        Example("""let arr = [1, 2, 3]; arr[1] = 99; arr""", "[1, 99, 3]"),
        Example("""let obj = {a: 1}; obj.b = 2; obj""", "{a: 1, b: 2}"),
        Example("""let obj = {a: {b: 1}}; obj.a.b = 42; obj.a.b""", "42"),
        Example("""let obj = {count: 0}; obj.count += 5; obj.count""", "5",
          "Compound assignment on fields"),
      )),

      Section("Destructuring", """
Object and array destructuring work in `let` declarations and `for` loops.
      """.trim(), listOf(
        Example("""let {name, age} = {name: "alice", age: 30}; name""", "alice"),
        Example("""let [a, b, c] = [10, 20, 30]; b""", "20"),
        Example("""let {name: n} = {name: "bob"}; n""", "bob",
          "Rename during destructure"),
      )),

      Section("Spread Operator", """
Spread (`...`) works in arrays and objects to merge or extend values.
      """.trim(), listOf(
        Example("""let a = [1, 2]; [...a, 3, 4]""", "[1, 2, 3, 4]"),
        Example("""let a = {x: 1}; {...a, y: 2}""", "{x: 1, y: 2}"),
      )),

      Section("Object Operations", """
Inspect and transform objects with keys, values, entries.
      """.trim(), listOf(
        Example("""{a: 1, b: 2} |> keys()""", """["a", "b"]"""),
        Example("""{a: 1, b: 2} |> values()""", "[1, 2]"),
        Example("""{a: 1, b: 2} |> entries()""", """[{key: "a", value: 1}, {key: "b", value: 2}]"""),
      )),

      Section("Math", """
Numeric operations for rounding, clamping, and power.
      """.trim(), listOf(
        Example("""3.7 |> floor()""", "3"),
        Example("""3.2 |> ceil()""", "4"),
        Example("""3.5 |> round()""", "4"),
        Example("""-5 |> abs()""", "5"),
        Example("""min(3, 7)""", "3"),
        Example("""max(3, 7)""", "7"),
        Example("""pow(2, 10)""", "1024"),
      )),

      Section("Type Conversion and Inspection", """
Convert between types and inspect values.
      """.trim(), listOf(
        Example("""str(42)""", "42"),
        Example("""num("3.14")""", "3.14"),
        Example("""typeof(42)""", "number"),
        Example("""typeof("hello")""", "string"),
        Example("""typeof([1, 2])""", "array"),
        Example("""typeof({a: 1})""", "object"),
        Example("""[1, 2, 3] |> len()""", "3"),
      )),

      Section("JSON", """
Parse and serialize JSON. Works with pipes.
      """.trim(), listOf(
        Example("""parseJson("[1, 2, 3]")""", "[1, 2, 3]"),
        Example("""parseJson("{\"name\": \"alice\"}")""", """{name: "alice"}"""),
        Example("""{a: 1, b: [2, 3]} |> toJson()""", """{"a":1,"b":[2,3]}"""),
      )),

      Section("Ternary and Null Coalescing", """
Conditional expressions and null handling.
      """.trim(), listOf(
        Example("""true ? "yes" : "no" """, "yes"),
        Example("""let x = null; x ?? "default" """, "default"),
        Example("""let x = 42; x ?? "default" """, "42"),
      )),

      Section("Optional Chaining", """
Safe property access on potentially null values.
      """.trim(), listOf(
        Example("""let x = {a: {b: 42}}; x?.a?.b""", "42"),
        Example("""let x = null; x?.a?.b""", "null"),
      )),

      Section("Composition", """
`chain` sequences functions. `all` runs functions and collects results.
`race` returns the first successful result.
      """.trim(), listOf(
        Example("""chain(() => [1, 2, 3], arr => arr |> map(x => x * 2), arr => arr |> join(", "))""",
          "2, 4, 6"),
        Example("""all(() => 1 + 2, () => 3 * 4)""", "[3, 12]"),
        Example("""race(() => fail("down"), () => "fallback")""", "fallback"),
      )),

      Section("Scatter Pipe and toArray", """
The scatter pipe `|*` normalizes the left side to an array (`null` becomes `[]`,
a non-array becomes `[value]`), then runs the right-side function on each element
**in parallel**, returning an array of results. Use `toArray()` to normalize a
value to an array without mapping.
      """.trim(), listOf(
        Example("""[1, 2, 3] |* (x => x * 10)""", "[10, 20, 30]",
          "Scatter: parallel map over array"),
        Example("""null |* (x => x * 2)""", "[]",
          "Scatter: null becomes empty array"),
        Example("""5 |* (x => x * 2)""", "[10]",
          "Scatter: non-array wrapped then mapped"),
        Example("""[1, 2, 3] |* (x => x * 2) |> reduce((sum, x) => sum + x)""", "12",
          "Scatter then reduce"),
        Example("""5 |> toArray()""", "[5]",
          "toArray: wraps non-array"),
        Example("""null |> toArray()""", "[]",
          "toArray: null becomes empty array"),
      )),

      Section("Assertions", """
`assert` checks a condition and fails with a message if it's falsy.
Use it to enforce invariants on data, graph structure, or any intermediate result.
      """.trim(), listOf(
        Example("""assert("math works", 2 + 2 == 4)""", "null"),
        Example("""let items = [1, 2, 3]; assert("has items", items |> len() > 0)""", "null"),
      )),

      Section("REPL Pattern", """
State persists across `eval` calls. Functions and variables from earlier evals
are available in later ones.
      """.trim(), listOf(
        Example("""let x = 42""", "null", "Define in one eval"),
      )),

      Section("Graph Toolkit", """
The graph toolkit provides a property graph with typed nodes, typed edges, and
pipe-based traversal. All nodes must connect to root. Traversal steps (`out`,
`inbound`, `both`) compose naturally with `filter`, `map`, `reduce`.
      """.trim(), listOf(
        Example("""
let alice = addNode(root(), "person", {name: "Alice", age: 30})
let bob = addNode(root(), "person", {name: "Bob", age: 25})
link(alice, bob, "knows")
root() |> out("person") |> map(n => n.name) |> sort()
        """.trim(), """["Alice", "Bob"]""", "Create nodes and traverse"),
        Example("""
let alice = addNode(root(), "person", {name: "Alice"})
let acme = addNode(root(), "company", {name: "Acme"})
link(alice, acme, "worksAt")
let proj = addNode(acme, "project", {name: "TShell"})
link(alice, proj, "leads")
nodes("person") |> out("worksAt") |> map(n => n.name)
        """.trim(), """["Acme"]""", "Typed edges for relationships"),
        Example("""
let a = addNode(root(), "person", {name: "A"})
let b = addNode(root(), "person", {name: "B"})
link(a, b, "friend")
nodes("person") |> filter(n => n.name == "B") |> inbound("friend") |> map(n => n.name)
        """.trim(), """["A"]""", "Reverse traversal with inbound"),
        Example("""
addNode(root(), "item", {value: 10})
addNode(root(), "item", {value: 20})
addNode(root(), "item", {value: 30})
root() |> out("item") |> map(n => n.value) |> reduce((sum, v) => sum + v, 0)
        """.trim(), "60", "Aggregate over traversal results"),
      )),

      Section("Error Handling", """
tshell provides clear error messages with suggestions. Unknown commands show
similar names. Type mismatches show what was expected. Execution limits prevent
runaway programs.
      """.trim(), listOf()),
    )),
  )

  // Flat view for test runner
  private val sections = groups.flatMap { it.sections }

  // --- Run all examples as tests ---

  @Test fun `all literate examples produce expected output`() {
    val failures = mutableListOf<String>()

    for (section in sections) {
      for (example in section.examples) {
        val sh = if (section.title.contains("Graph")) graphShell() else shell()
        try {
          val result = sh.eval(example.code)
          val actual = result.toDisplayString()
          if (actual != example.expect.trim()) {
            failures.add(
              "${section.title}: ${example.description.ifEmpty { example.code.lines().first() }}\n" +
                "  expected: ${example.expect.trim()}\n" +
                "  actual:   $actual"
            )
          }
        } catch (e: Exception) {
          failures.add(
            "${section.title}: ${example.description.ifEmpty { example.code.lines().first() }}\n" +
              "  threw: ${e.message?.lines()?.first()}"
          )
        }
      }
    }

    if (failures.isNotEmpty()) {
      fail<Unit>("${failures.size} literate example(s) failed:\n\n${failures.joinToString("\n\n")}")
    }
  }

  // --- Generate README ---

  @Test fun `generate README from literate examples`() {
    val readme = buildString {
      appendLine("# tshell")
      appendLine()
      appendLine("A sandboxed scripting language that gives LLMs safe, expressive computation")
      appendLine("through a single `eval` tool.")
      appendLine()
      appendLine("Instead of defining dozens of individual tools — each requiring schema,")
      appendLine("validation, and prompt engineering — give your LLM one tool: `eval`. tshell's")
      appendLine("TypeScript-like syntax means LLMs can write it without new training.")
      appendLine("Capabilities are discovered at runtime via `help()`, and the KV cache is")
      appendLine("preserved across tool set changes.")
      appendLine()

      for (group in groups) {
        appendLine("## ${group.title}")
        appendLine()
        if (group.prose.isNotEmpty()) {
          appendLine(group.prose)
          appendLine()
        }

        for (section in group.sections) {
          appendLine("### ${section.title}")
          appendLine()
          appendLine(section.prose)
          appendLine()

          for (example in section.examples) {
            if (example.description.isNotEmpty()) {
              appendLine("${example.description}:")
              appendLine()
            }
            appendLine("```javascript")
            appendLine(example.code.trimIndent())
            appendLine("// → ${example.expect.trim()}")
            appendLine("```")
            appendLine()
          }
        }
      }

      appendLine("## Integration")
      appendLine()
      appendLine("tshell is designed for text-in, text-out integration. Your host application")
      appendLine("sends tshell source code as a string and receives results as a string. State")
      appendLine("persists across calls, enabling multi-step interactions.")
      appendLine()
      appendLine("### Kotlin / JVM")
      appendLine()
      appendLine("```kotlin")
      appendLine("val shell = TShell()")
      appendLine("CoreToolkit.install(shell)")
      appendLine()
      appendLine("// Text in → text out")
      appendLine("""val result = shell.eval("[1, 2, 3] |> map(x => x * 2)")""")
      appendLine("""println(result.toDisplayString()) // "[2, 4, 6]"""")
      appendLine()
      appendLine("// State persists across evals")
      appendLine("""shell.eval("let x = 42")""")
      appendLine("""shell.eval("x")  // → 42""")
      appendLine()
      appendLine("// Transactional eval — rolls back on failure")
      appendLine("""shell.evalTransactional("x = 99; fail(\"boom\")")""")
      appendLine("""shell.eval("x")  // → 42 (unchanged)""")
      appendLine("```")
      appendLine()
      appendLine("### Adding Capabilities")
      appendLine()
      appendLine("```kotlin")
      appendLine("// Graph toolkit")
      appendLine("GraphToolkit(InMemoryGraphStore()).install(shell)")
      appendLine()
      appendLine("// Load toolkit extensions from a directory")
      appendLine("""shell.loadToolkits(Path.of(".tsh"))""")
      appendLine()
      appendLine("// Namespaced loading")
      appendLine("""shell.loadToolkits(Path.of(".tsh"), mapOf("graph" to "g"))""")
      appendLine("// Now: g.addNode(...), g.out(...)")
      appendLine("```")
      appendLine()
      appendLine("### LLM System Prompt Generation")
      appendLine()
      appendLine("```kotlin")
      appendLine("// Generate a system prompt describing all available commands")
      appendLine("val prompt = shell.toPrompt()")
      appendLine("```")
      appendLine()
      appendLine("The generated prompt includes syntax, available commands, and their")
      appendLine("signatures — everything an LLM needs to write tshell code.")
      appendLine()

      appendLine("### Execution Limits")
      appendLine()
      appendLine("tshell sandboxes execution to prevent runaway programs.")
      appendLine()
      appendLine("```kotlin")
      appendLine("val shell = TShell(")
      appendLine("  maxSteps = 1_000_000,   // max execution steps (default)")
      appendLine("  maxCallDepth = 256,     // max recursion depth (default)")
      appendLine("  timeoutMs = 30_000      // wall-clock timeout in ms (default)")
      appendLine(")")
      appendLine("```")
      appendLine()
      appendLine("From within tshell, `limits()` shows current limits and `extendLimit({steps: n, timeout: ms, callDepth: n, outputBytes: n})`")
      appendLine("can increase them.")
      appendLine()
      appendLine("### State Serialization")
      appendLine()
      appendLine("Save and restore session state for multi-turn interactions.")
      appendLine()
      appendLine("```kotlin")
      appendLine("// Capture all global bindings")
      appendLine("val state = shell.getState()")
      appendLine()
      appendLine("// Restore into a new shell instance")
      appendLine("val newShell = TShell()")
      appendLine("CoreToolkit.install(newShell)")
      appendLine("newShell.setState(state)")
      appendLine()
      appendLine("// Or inject specific bindings")
      appendLine("""newShell.setState(mapOf("user" to TShellValue.TString("alice")))""")
      appendLine("```")
      appendLine()

      appendLine("## Extending tshell")
      appendLine()
      appendLine("Register custom commands from your host language, or package reusable")
      appendLine("functions as toolkit extensions.")
      appendLine()
      appendLine("### Custom Commands (Kotlin)")
      appendLine()
      appendLine("```kotlin")
      appendLine("shell.register(")
      appendLine("""  "greet", "name: string",""")
      appendLine("""  "returns a greeting for the given name",""")
      appendLine("""  listOf("greet(\"world\")")""")
      appendLine(") { args ->")
      appendLine("""  val name = (args[0] as TShellValue.TString).value""")
      appendLine("""  TShellValue.TString("hello ${'$'}name")""")
      appendLine("}")
      appendLine()
      appendLine("""// Now available in tshell: greet("world") // → "hello world"""")
      appendLine("```")
      appendLine()
      appendLine("Commands registered this way appear in `help()` with their signature,")
      appendLine("description, and examples.")
      appendLine()
      appendLine("### Toolkit Extensions (.tshell files)")
      appendLine()
      appendLine("Package reusable functions as `.tshell` files in a directory structure:")
      appendLine()
      appendLine("```")
      appendLine(".tsh/")
      appendLine("  math/")
      appendLine("    stats.tshell           # function mean(arr) { ... }")
      appendLine("    typical_usage.md       # shown in help(\"math\")")
      appendLine("    advanced_usage.md")
      appendLine("  text/")
      appendLine("    nlp.tshell")
      appendLine("    typical_usage.md")
      appendLine("```")
      appendLine()
      appendLine("```kotlin")
      appendLine("// Load all toolkits — functions become global")
      appendLine("""shell.loadToolkits(Path.of(".tsh"))""")
      appendLine()
      appendLine("// Or namespace them to avoid collisions")
      appendLine("""shell.loadToolkits(Path.of(".tsh"), mapOf("math" to "m"))""")
      appendLine("// m.mean([1, 2, 3]) instead of mean([1, 2, 3])")
      appendLine("```")
      appendLine()
      appendLine("Toolkit files are evaluated in alphabetical order. Prefix with numbers")
      appendLine("(`01_base.tshell`, `02_derived.tshell`) to control load order when files")
      appendLine("depend on each other.")
      appendLine()
      appendLine("## Optional Modules")
      appendLine()
      appendLine("tshell ships with optional modules that can be installed independently.")
      appendLine("Only `CoreToolkit` is required.")
      appendLine()
      appendLine("| Module |> Install |> Provides |")
      appendLine("| --- |> --- |> --- |")
      appendLine("| **CoreToolkit** |> `CoreToolkit.install(shell)` |> Pipes, arrays, strings, math, JSON, composition |")
      appendLine("| **GraphToolkit** |> `GraphToolkit(store).install(shell)` |> Property graph with typed nodes/edges and traversal |")
      appendLine("| **FileToolkit** |> `FileToolkit(rootPath).install(shell)` |> Sandboxed file I/O: read, write, glob, grep, edit |")
      appendLine()
      appendLine("```kotlin")
      appendLine("// Minimal setup — just the language + core functions")
      appendLine("val shell = TShell()")
      appendLine("CoreToolkit.install(shell)")
      appendLine()
      appendLine("// Add file operations (sandboxed to a root directory)")
      appendLine("FileToolkit(Path.of(\"/workspace\")).install(shell)")
      appendLine()
      appendLine("// Add graph operations")
      appendLine("GraphToolkit(InMemoryGraphStore()).install(shell)")
      appendLine("```")
      appendLine()
      appendLine("### Graph Schema Enforcement")
      appendLine()
      appendLine("Define a schema to prevent LLMs from adding invalid data to the graph.")
      appendLine("Schemas are enforced eagerly — `addNode`, `link`, and `setProps` fail")
      appendLine("immediately if they violate constraints. The LLM cannot modify the schema.")
      appendLine()
      appendLine("```kotlin")
      appendLine("val schema = GraphSchema(")
      appendLine("  nodes = mapOf(")
      appendLine("""    "person" to NodeSchema(required = setOf("name"), optional = setOf("age", "email")),""")
      appendLine("""    "company" to NodeSchema(required = setOf("name")),""")
      appendLine("  ),")
      appendLine("  edges = mapOf(")
      appendLine("""    "worksAt" to EdgeSchema(from = "person", to = "company"),""")
      appendLine("""    "knows" to EdgeSchema(from = "person", to = "person"),""")
      appendLine("  ),")
      appendLine("  strict = true // reject unknown node/edge types entirely")
      appendLine(")")
      appendLine()
      appendLine("GraphToolkit(InMemoryGraphStore(), schema = schema).install(shell)")
      appendLine("```")
      appendLine()
      appendLine("With this schema in place:")
      appendLine()
      appendLine("```javascript")
      appendLine("""addNode(root(), "person", {age: 30})        // ERROR: missing required 'name'""")
      appendLine("""addNode(root(), "alien", {name: "X"})       // ERROR: unknown node type (strict)""")
      appendLine("""link(company, person, "worksAt")             // ERROR: worksAt must originate from 'person'""")
      appendLine("```")
      appendLine()
      appendLine("The schema is automatically included in `help(\"graph\")` so LLMs know")
      appendLine("what types and properties are valid.")
      appendLine()

      appendLine("---")
      appendLine()
      appendLine("*This README is generated from")
      appendLine("[`TShellLiterateTest.kt`](src/test/kotlin/com/iodesystems/tshell/TShellLiterateTest.kt).")
      appendLine("Every code example above is executed as part of the test run — if an example")
      appendLine("is wrong, the build fails.*")
    }

    val readmePath = Path.of("").toAbsolutePath().resolve("README.md")
    readmePath.writeText(readme)
  }
}
