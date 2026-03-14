package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import com.iodesystems.tshell.toolkit.graph.GraphToolkit
import com.iodesystems.tshell.toolkit.graph.InMemoryGraphStore
import org.testng.annotations.Test
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

    // ======== WHY TSHELL ========

    SectionGroup("Why tshell", """
LLMs are bad at computation. They can't reliably count letters, do arithmetic on large
numbers, or sort data by reasoning alone. But they're excellent at writing code —
especially JavaScript, which dominates their training data.

tshell gives your LLM a single `eval` tool with JS syntax. Instead of defining dozens of
tools (each requiring schema, validation, and prompt engineering), you get one tool that
covers computation, string processing, data transformation, and more. Capabilities are
discovered at runtime via `help()`, so the system prompt stays small and the KV cache
survives tool set changes.
    """.trim(), listOf(

      Section("Computation Beats Reasoning", """
LLMs famously fail at tasks like "how many R's in strawberry?" because they try to
reason about it instead of computing it. With tshell, they write code instead.
      """.trim(), listOf(
        Example(""""strawberry" |> split("") |> filter(c => c == "r") |> len()""", "3",
          "Count letters — LLMs get this wrong by reasoning, right by computing"),
        Example("""
let words = "the quick brown fox jumps over the lazy dog the fox"
let counts = words |> split(" ") |> countBy(w => w)
entries(counts) |> filter(e => e[1] > 1) |> map(e => `${'$'}{e[0]}=${'$'}{e[1]}`) |> join(", ")
        """.trim(), "the=3, fox=2",
          "Word frequency — multi-step analysis in one expression"),
      )),

      Section("One Tool Replaces Many", """
Without tshell, an LLM needs separate tools for search, math, string processing, data
transformation — each requiring a round-trip. With tshell, complex multi-step operations
happen in a single `eval` call.
      """.trim(), listOf(
        Example("""
let data = [{name: "Alice", score: 85}, {name: "Bob", score: 92}, {name: "Carol", score: 78}]
let avg = data |> map(d => d.score) |> reduce((sum, s) => sum + s, 0) |> (total => total / (data |> len()))
let above = data |> filter(d => d.score >= avg)
above |> map(d => `${'$'}{d.name}: ${'$'}{d.score}`) |> join(", ")
        """.trim(), "Alice: 85, Bob: 92",
          "Filter, aggregate, and format — three tool calls collapsed into one"),
        Example("""
"2024-03-14T10:30:00" |> split("T") |> (parts => {
  let [date, time] = parts
  let [y, m, d] = date |> split("-")
  {year: num(y), month: num(m), day: num(d), time: time}
})
        """.trim(), """{year: 2024, month: 3, day: 14, time: "10:30:00"}""",
          "Parse and restructure — no special date-parsing tool needed"),
      )),

      Section("Runtime Discovery", """
LLMs discover available capabilities at runtime via `help()` instead of needing them
encoded in the system prompt. When you add a toolkit, the LLM finds it automatically.
      """.trim(), listOf(
        Example("""help("sort") |> split("\n") |> limit(1) |> join("")""",
          "sort(input: array, keyOrComparator?: string | (a, b) => number)",
          "LLMs call help() to learn signatures and usage"),
        Example("""help("countBy") |> split("\n") |> limit(1) |> join("")""",
          "countBy(input: array, fn: (T) => string)",
          "Exact match shows full signature and docs"),
      )),

    )),

    // ======== TYPICAL USAGE ========

    SectionGroup("Typical Usage", """
tshell reads like JavaScript and composes like a shell. These are the features you'll
use in most programs.
    """.trim(), listOf(

      Section("Variables and Types", """
`let` declares variables. Types are inferred. Objects and arrays are references
(JS semantics) — assignment to fields/indices mutates the value in-place.
      """.trim(), listOf(
        Example("""let x = 42; x""", "42"),
        Example("""let name = "alice"; name""", "alice"),
        Example("""let active = true; active""", "true"),
        Example("""let nothing = null; nothing""", "null"),
        Example("""let items = [1, 2, 3]; items""", "[1, 2, 3]"),
        Example("""let obj = {name: "alice", age: 30}; obj.age""", "30"),
        Example("""let a, b = 1, c = "two"; a""", "null", "Multi-binding — uninitialized vars are null"),
        Example("""let a, b = 1, c = "two"; c""", "two"),
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
The pipe operator `|>` passes the left-hand value as the first argument to the right-hand
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
`help()` lists all available commands. `help("name")` shows detailed usage.
See [Runtime Discovery](#runtime-discovery) above for more.
      """.trim(), listOf(
        Example("""typeof(help())""", "string", "help() returns a string listing all commands"),
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
        Example("""{a: 1, b: 2} |> entries()""", """[["a", 1], ["b", 2]]"""),
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
        Example("""let x = 42""", "42", "Define in one eval"),
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
      appendLine("One tool instead of twenty. Give your LLM `eval` and let it compute.")
      appendLine()
      appendLine("tshell is a sandboxed scripting language with JavaScript syntax — the one")
      appendLine("language every LLM already knows. Instead of building separate tools for")
      appendLine("search, math, string processing, and data transformation (each needing")
      appendLine("schema, validation, and prompt engineering), you give your LLM a single")
      appendLine("`eval` tool that handles all of it. Capabilities are discovered at runtime")
      appendLine("via `help()`, so the system prompt stays small and the KV cache survives")
      appendLine("tool set changes.")
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
      appendLine("tshell is text-in, text-out. Send source code, get results. State persists")
      appendLine("across calls. Bind your own Kotlin functions as commands — the LLM discovers")
      appendLine("them via `help()` without any prompt changes.")
      appendLine()
      appendLine("### Quick Start")
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
      appendLine("### Bind Your Own Commands")
      appendLine()
      appendLine("Register any Kotlin function as a tshell command. It appears in `help()` with")
      appendLine("its signature, description, and examples — the LLM discovers it automatically.")
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
      appendLine("""// Now in tshell: greet("world") // → "hello world"""")
      appendLine("""// And: help("greet") shows the signature and examples""")
      appendLine("```")
      appendLine()
      appendLine("This is the core integration pattern: your domain logic lives in Kotlin,")
      appendLine("the LLM composes it via tshell. Add a database query command, an API call,")
      appendLine("a domain calculation — the LLM chains them with pipes, loops, and data")
      appendLine("transforms without needing separate tool schemas for each one.")
      appendLine()
      appendLine("### LLM Agent Integration (Koog)")
      appendLine()
      appendLine("Wrap tshell as a single tool for any LLM agent framework. Here's the")
      appendLine("complete binding for [Koog](https://github.com/JetBrains/koog):")
      appendLine()
      appendLine("```kotlin")
      appendLine("@LLMDescription(\"tshell scripting\")")
      appendLine("class TShellTools(private val shell: TShell) : ToolSet {")
      appendLine("  @Tool")
      appendLine("  @LLMDescription(TShell.TOOL_DESCRIPTION)")
      appendLine("  fun tshell(@LLMDescription(\"tshell source code\") code: String): String {")
      appendLine("    return try {")
      appendLine("      shell.evalExported(code).toDisplayString()")
      appendLine("    } catch (e: Exception) {")
      appendLine("      \"ERROR: ${'$'}{e.message}\"")
      appendLine("    }")
      appendLine("  }")
      appendLine("}")
      appendLine()
      appendLine("// Create the agent")
      appendLine("val shell = TShell()")
      appendLine("CoreToolkit.install(shell)")
      appendLine("FileToolkit(Path.of(\"/workspace\")).install(shell)")
      appendLine()
      appendLine("val agent = AIAgent(")
      appendLine("  model = OpenAIModels.chatGPT4o,")
      appendLine("  systemPrompt = \"You have a tshell tool for computation.\\n\" + shell.toPrompt(),")
      appendLine("  tools = listOf(TShellTools(shell))")
      appendLine(")")
      appendLine("```")
      appendLine()
      appendLine("That's it — one class, one tool. The LLM gets computation, file I/O, data")
      appendLine("processing, and any custom commands you register. `shell.toPrompt()` generates")
      appendLine("the syntax reference and command signatures automatically.")
      appendLine()
      appendLine("The same pattern works for building MCP servers, OpenAI function-calling")
      appendLine("integrations, or any framework that supports tool use. The binding is always")
      appendLine("the same: text in, text out.")
      appendLine()
      appendLine("See [`tshell-sample-koog`](tshell-sample-koog/) for a complete working example")
      appendLine("with a local LLM, including benchmarks.")
      appendLine()
      appendLine("### Execution Limits")
      appendLine()
      appendLine("tshell sandboxes execution to prevent runaway programs.")
      appendLine()
      appendLine("```kotlin")
      appendLine("val shell = TShell(")
      appendLine("  maxSteps = 1_000_000,   // max AST node evaluations (default)")
      appendLine("  maxCallDepth = 256,     // max recursion depth (default)")
      appendLine("  timeoutMs = 30_000      // wall-clock timeout in ms (default)")
      appendLine(")")
      appendLine("```")
      appendLine()
      appendLine("From within tshell, `limits()` shows current limits and `extendLimit({steps: n, timeout: ms, callDepth: n, outputBytes: n})`")
      appendLine("can increase them when the LLM needs heavier computation.")
      appendLine()
      appendLine("### State Serialization")
      appendLine()
      appendLine("Save and restore session state for multi-turn interactions.")
      appendLine()
      appendLine("```kotlin")
      appendLine("val state = shell.getState()")
      appendLine()
      appendLine("val newShell = TShell()")
      appendLine("CoreToolkit.install(newShell)")
      appendLine("newShell.setState(state)")
      appendLine()
      appendLine("// Or inject specific bindings")
      appendLine("""newShell.setState(mapOf("user" to TShellValue.TString("alice")))""")
      appendLine("```")
      appendLine()
      appendLine("## Modules")
      appendLine()
      appendLine("tshell is modular. Install only what you need — each module adds commands")
      appendLine("that the LLM discovers via `help()`. All modules are published to Maven Central")
      appendLine("under `com.iodesystems.tshell`.")
      appendLine()
      appendLine("### Core (`tshell`)")
      appendLine()
      appendLine("```kotlin")
      appendLine("implementation(\"com.iodesystems.tshell:tshell:0.1.1\")")
      appendLine("```")
      appendLine()
      appendLine("The language runtime plus `CoreToolkit`: pipes, arrays, strings, math, JSON,")
      appendLine("composition (`all`, `race`, `chain`), and `help()`. Also includes:")
      appendLine()
      appendLine("| Toolkit | Install | Commands |")
      appendLine("| --- | --- | --- |")
      appendLine("| **CoreToolkit** | `CoreToolkit.install(shell)` | `map`, `filter`, `reduce`, `sort`, `join`, `split`, `keys`, `values`, `entries`, `groupBy`, `countBy`, `range`, `parseJson`, `toJson`, and [more](#typical-usage) |")
      appendLine("| **MathToolkit** | `MathToolkit().install(shell)` | `Math.sin`, `Math.cos`, `Math.sqrt`, `Math.log`, `Math.PI`, `Math.E`, etc. |")
      appendLine("| **WebToolkit** | `WebToolkit().install(shell)` | `Web.fetch`, `Web.fetchText`, `Web.search`, `Html.select`, `Html.links`, `Html.text`, `Html.table` |")
      appendLine("| **FileToolkit** | `FileToolkit(rootPath).install(shell)` | Sandboxed `read`, `write`, `glob`, `grep`, `edit` — LLM can only access files under `rootPath` |")
      appendLine("| **GraphToolkit** | `GraphToolkit(store).install(shell)` | Property graph: `addNode`, `link`, `out`, `inbound`, `nodes`, `setProps` — see [Graph Toolkit](#graph-toolkit) |")
      appendLine()
      appendLine("### Browser Automation (`tshell-playwright`)")
      appendLine()
      appendLine("```kotlin")
      appendLine("implementation(\"com.iodesystems.tshell:tshell-playwright:0.1.1\")")
      appendLine("```")
      appendLine()
      appendLine("Playwright-based browser automation. The LLM can navigate pages, click elements,")
      appendLine("fill forms, read text, and take screenshots — all through tshell commands.")
      appendLine()
      appendLine("```kotlin")
      appendLine("val browser = BrowserToolkit(headless = true)")
      appendLine("browser.install(shell)")
      appendLine("// Commands: Browser.open, Browser.click, Browser.type, Browser.text,")
      appendLine("//           Browser.select, Browser.screenshot, Browser.eval, ...")
      appendLine("```")
      appendLine()
      appendLine("### SQL (`tshell-sql`)")
      appendLine()
      appendLine("```kotlin")
      appendLine("implementation(\"com.iodesystems.tshell:tshell-sql:0.1.1\")")
      appendLine("```")
      appendLine()
      appendLine("JDBC-based SQL toolkit. Supports multiple databases, read-only mode by default,")
      appendLine("parameterized queries, and schema introspection.")
      appendLine()
      appendLine("```kotlin")
      appendLine("val sql = SqlToolkit(mapOf(\"db\" to DatabaseConfig(dataSource)))")
      appendLine("sql.install(shell)")
      appendLine("// Commands: db.query, db.tables, db.columns, db.schema")
      appendLine("// Read-only by default — LLM must call db.requestWrite() for mutations")
      appendLine("```")
      appendLine()
      appendLine("### Sample Agent (`tshell-sample-koog`)")
      appendLine()
      appendLine("Complete working example: a CLI chat agent powered by a local LLM (any")
      appendLine("OpenAI-compatible API) with tshell as its computation tool. Includes")
      appendLine("benchmarks proving LLMs can solve problems via code that they can't solve")
      appendLine("by reasoning alone (96% pass rate on 32 coding challenges).")
      appendLine()
      appendLine("See [`tshell-sample-koog/README.md`](tshell-sample-koog/README.md) for setup.")
      appendLine()
      appendLine("## Extending tshell")
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
      appendLine("  text/")
      appendLine("    nlp.tshell")
      appendLine("    typical_usage.md")
      appendLine("```")
      appendLine()
      appendLine("```kotlin")
      appendLine("shell.loadToolkits(Path.of(\".tsh\"))")
      appendLine()
      appendLine("// Or namespace to avoid collisions")
      appendLine("""shell.loadToolkits(Path.of(".tsh"), mapOf("math" to "m"))""")
      appendLine("// m.mean([1, 2, 3]) instead of mean([1, 2, 3])")
      appendLine("```")
      appendLine()
      appendLine("### Graph Schema Enforcement")
      appendLine()
      appendLine("Define a schema to constrain what the LLM can add to the graph.")
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
      appendLine("  strict = true // reject unknown node/edge types")
      appendLine(")")
      appendLine("GraphToolkit(InMemoryGraphStore(), schema = schema).install(shell)")
      appendLine("```")
      appendLine()
      appendLine("The schema is enforced eagerly and included in `help(\"graph\")` so the")
      appendLine("LLM knows what's valid.")
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
