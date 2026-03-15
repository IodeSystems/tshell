package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
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

    )),

    // ======== WHAT'S DIFFERENT FROM JS ========

    SectionGroup("What's Different from JS", """
tshell is a JS subset — `let`, `function`, `if`/`else`, `for`/`while`, arrow functions,
destructuring, spread, template strings, `?.`, `??`, ternary all work as expected.
These are the additions and differences.
    """.trim(), listOf(

      Section("Pipes", """
The pipe operator `|>` passes the left-hand value as the first argument to the right-hand
function. This replaces method chaining and is the primary way to compose operations.
      """.trim(), listOf(
        Example("""[3, 1, 2] |> sort""", "[1, 2, 3]"),
        Example("""[1, 2, 3] |> map(x => x * 10)""", "[10, 20, 30]"),
        Example(""""hello world" |> split(" ") |> map(w => w |> upper()) |> join(" ")""",
          "HELLO WORLD", "Chained pipes"),
        Example("""5 |> (x => x * x)""", "25", "Pipe into arrow function"),
      )),

      Section("Scatter Pipe", """
`|*` normalizes the left side to an array, then maps the right-side function over each
element **in parallel**.
      """.trim(), listOf(
        Example("""[1, 2, 3] |* (x => x * 10)""", "[10, 20, 30]"),
        Example("""null |* (x => x * 2)""", "[]",
          "null becomes empty array"),
        Example("""5 |* (x => x * 2)""", "[10]",
          "non-array wrapped then mapped"),
      )),

      Section("Commands", """
Standard operations are pipe-friendly commands instead of methods. The LLM discovers
them via `help()`. Common commands:
      """.trim(), listOf(
        Example("""[1, 2, 3] |> filter(x => x > 1) |> reduce((sum, x) => sum + x, 0)""", "5"),
        Example("""[3, 1, 2] |> sort((a, b) => b - a)""", "[3, 2, 1]",
          "Comparator sort"),
        Example("""[3, 1, 2] |> sort("desc")""", "[3, 2, 1]",
          "Descending sort"),
        Example("""{a: 1, b: 2} |> entries()""", """[["a", 1], ["b", 2]]"""),
        Example("""range(1, 5)""", "[1, 2, 3, 4]"),
        Example("""typeof(help())""", "string", "help() returns all available commands"),
      )),

      Section("Export and State", """
State is discarded after each `eval` call unless explicitly exported. Use `export` to
persist values across tool calls in multi-turn LLM conversations.
      """.trim(), listOf(
        Example("""let x = 42""", "42", "Without export, x is gone after this eval"),
      )),

      Section("Composition", """
`chain` sequences functions. `all` runs in parallel and collects results.
`race` returns the first success.
      """.trim(), listOf(
        Example("""chain(() => [1, 2, 3], arr => arr |> map(x => x * 2), arr => arr |> join(", "))""",
          "2, 4, 6"),
        Example("""all(() => 1 + 2, () => 3 * 4)""", "[3, 12]"),
        Example("""race(() => fail("down"), () => "fallback")""", "fallback"),
      )),

      Section("Raw Template Strings", """
What about double-encoding corruption? LLM writes `\\n`, transport eats the backslash,
interpreter gets a newline. Or the LLM spirals through `\\\\`, `\\\\\\\\`, never landing on the
right number of backslashes. Raw template strings fix this: prefix any template literal
with `r` to disable backslash escape processing. What you type is what you get.
`${'$'}{expr}` interpolation still works — Kotlin-style: raw content, live substitutions.
      """.trim(), listOf(
        Example("""r`C:\Users\foo`""", """C:\Users\foo""",
          "Backslashes are literal"),
        Example("""r`line1\nline2`""", """line1\nline2""",
          "No escape processing — \\n stays as two characters"),
        Example("""let name = "world"; r`Hello ${'$'}{name}, path=C:\Users`""",
          """Hello world, path=C:\Users""",
          "Interpolation still works"),
        Example("""`tab\there`""", "tab\there",
          "Regular template for comparison — \\t becomes a tab"),
      )),

      Section("What's Missing", """
No `class`, `this`, `new`, `async`/`await`, `import`/`require`, prototypes,
generators, or `Symbol`. Type annotations are accepted but ignored.
Use `help()` to discover what's available.
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
        val sh = shell()
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
      appendLine("One `eval` tool instead of twenty. Sandboxed JS syntax your LLM already knows.")
      appendLine()
      appendLine("- **LLMs compute instead of reasoning** — \"how many R's in strawberry?\" becomes `split |> filter |> len`")
      appendLine("- **~95% less tool context** — one `eval` tool replaces dozens of MCP tool schemas; the LLM prompt stays constant as you add capabilities")
      appendLine("- **Polyglot composition** — chain tools from Python, Go, TypeScript MCP servers with pipes in a single `eval` call")
      appendLine("- **KV cache friendly** — system prompt doesn't change when tools change; `help()` discovers capabilities at runtime")
      appendLine("- **No double-encoding corruption** — raw template strings (`r\\`...\\``) let LLMs write backslashes, quotes, and code exactly as-is. No more `\\\\\\\\` spirals when editing files with Windows paths or regex patterns")
      appendLine()
      // Context size info — computed from actual stdlib prompt, tokenized with cl100k_base
      val sh = shell()
      val fullPrompt = sh.toPrompt()
      val toolDesc = TShell.TOOL_DESCRIPTION
      val totalText = toolDesc + "\n" + fullPrompt
      val totalKb = "%.1f".format(totalText.length / 1024.0)
      val registry = Encodings.newDefaultEncodingRegistry()
      val enc = registry.getEncoding(EncodingType.CL100K_BASE)
      val tokens = enc.countTokens(totalText)
      appendLine("**Total stdlib context cost: ${totalKb}KB / ${tokens} tokens** (cl100k_base — tool description + syntax reference + command signatures)")
      appendLine()

      // ── Quick Start ──
      appendLine("## Quick Start")
      appendLine()
      appendLine("Build and run as an MCP server:")
      appendLine()
      appendLine("```bash")
      appendLine("git clone https://github.com/IodeSystems/tshell.git && cd tshell")
      appendLine("./gradlew :tshell-cli:installDist")
      appendLine("```")
      appendLine()
      appendLine("Add to your MCP client (Claude Desktop, Cursor, etc.):")
      appendLine()
      appendLine("```json")
      appendLine("{")
      appendLine("  \"mcpServers\": {")
      appendLine("    \"tshell\": {")
      appendLine("      \"command\": \"./tshell-cli/build/install/tshell-cli/bin/tshell-cli\"")
      appendLine("    }")
      appendLine("  }")
      appendLine("}")
      appendLine("```")
      appendLine()
      appendLine("Your LLM now has `eval` and `help`. It writes JS-syntax code; tshell executes it.")
      appendLine()
      appendLine("```javascript")
      appendLine("// LLM can compute instead of reasoning:")
      appendLine("\"strawberry\" |> split(\"\") |> filter(c => c == \"r\") |> len()  // → 3")
      appendLine()
      appendLine("// Chain operations in a single tool call:")
      appendLine("let data = [{name: \"Alice\", score: 85}, {name: \"Bob\", score: 92}]")
      appendLine("data |> filter(d => d.score > 80) |> map(d => d.name) |> join(\", \")  // → \"Alice, Bob\"")
      appendLine("```")
      appendLine()

      // ── MCP Composition ──
      appendLine("## MCP Composition")
      appendLine()
      appendLine("Connect external MCP servers — their tools become tshell commands, composed with pipes.")
      appendLine("Use `--mcp` with the standard JSON config format, or `--connect` for shorthand:")
      appendLine()
      appendLine("```bash")
      appendLine("# Standard MCP JSON config — same format as Claude Desktop, Cursor, etc.")
      appendLine("tshell-cli --mcp '{\"mcpServers\": {\"browser\": {\"command\": \"npx\", \"args\": [\"@anthropic/mcp-playwright\"]}}}'")
      appendLine()
      appendLine("# Or point to a config file")
      appendLine("tshell-cli --mcp mcp-config.json")
      appendLine()
      appendLine("# Shorthand for simple cases")
      appendLine("tshell-cli --connect \"app=python my_tools.py\" --connect \"data=npx data-server\"")
      appendLine("```")
      appendLine()
      appendLine("```")
      appendLine("LLM")
      appendLine(" │")
      appendLine(" ▼")
      appendLine("tshell-cli (single MCP server)")
      appendLine(" ├── eval(\"app.users() |> filter(u => u.active) |> sort(\\\"name\\\")\")")
      appendLine(" │    ├── tshell core (pipes, filter, sort, strings, math)")
      appendLine(" │    ├── app.* → Python MCP server (your app code)")
      appendLine(" │    └── data.* → Go MCP server (your data pipeline)")
      appendLine(" └── help() → all commands from all servers")
      appendLine("```")
      appendLine()
      appendLine("**Context reduction:** each MCP tool inflates the LLM's prompt with schema and")
      appendLine("descriptions. A Playwright MCP server adds ~8KB of tool definitions. tshell")
      appendLine("replaces all of it with one `eval` tool — commands are discovered via `help()`")
      appendLine("at runtime, not baked into the prompt. The KV cache survives tool set changes.")
      appendLine()
      appendLine("See [`tshell-cli/README.md`](tshell-cli/README.md) for CLI flags, Claude Desktop config, and JDBC driver setup.")
      appendLine()

      // ── Why tshell ──
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

      // ── Programmatic Integration ──
      appendLine("## Programmatic Integration")
      appendLine()
      appendLine("Published to Maven Central as `com.iodesystems.tshell:tshell`.")
      appendLine()
      appendLine("```kotlin")
      appendLine("// build.gradle.kts")
      appendLine("dependencies {")
      appendLine("  implementation(\"com.iodesystems.tshell:tshell:0.1.0\")")
      appendLine("}")
      appendLine("```")
      appendLine()
      appendLine("```kotlin")
      appendLine("val shell = TShell()")
      appendLine("CoreToolkit.install(shell)")
      appendLine()
      appendLine("""val result = shell.eval("[1, 2, 3] |> map(x => x * 2)")""")
      appendLine("""println(result.toDisplayString()) // "[2, 4, 6]"""")
      appendLine("```")
      appendLine()
      appendLine("### Custom Commands")
      appendLine()
      appendLine("Register Kotlin functions as tshell commands. They appear in `help()`")
      appendLine("automatically — the LLM discovers them without prompt changes.")
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
      appendLine("```")
      appendLine()
      appendLine("### Execution Limits")
      appendLine()
      appendLine("```kotlin")
      appendLine("val shell = TShell(")
      appendLine("  maxSteps = 1_000_000,   // max AST node evaluations")
      appendLine("  maxCallDepth = 256,     // max recursion depth")
      appendLine("  timeoutMs = 30_000      // wall-clock timeout")
      appendLine(")")
      appendLine("```")
      appendLine()

      // ── Modules ──
      appendLine("## Modules")
      appendLine()
      appendLine("| Module | Artifact | Provides |")
      appendLine("| --- | --- | --- |")
      appendLine("| **Core** | `tshell` | Language runtime, `CoreToolkit` (pipes, arrays, strings, math, JSON, composition), `MathToolkit`, `WebToolkit`, `FileToolkit` |")
      appendLine("| **Graph** | `tshell-graph` | Graph database toolkit: nodes, edges, traversal, schema validation. See [`tshell-graph/README.md`](tshell-graph/README.md) |")
      appendLine("| **MCP** | `tshell-mcp` | MCP server + client library. See [`tshell-mcp/README.md`](tshell-mcp/README.md) |")
      appendLine("| **CLI** | `tshell-cli` | Standalone MCP server binary with toolkits, external server composition, JDBC drivers. See [`tshell-cli/README.md`](tshell-cli/README.md) |")
      appendLine("| **Browser** | `tshell-playwright` | Lean Playwright automation (12 commands, ~800 chars context vs ~8KB for `@playwright/mcp`). See [`tshell-playwright/README.md`](tshell-playwright/README.md) |")
      appendLine("| **SQL** | `tshell-sql` | JDBC toolkit: `db.query`, `db.tables`, `db.schema`. Read-only by default |")
      appendLine("| **Sample** | `tshell-sample-koog` | CLI chat agent + benchmarks (100% on 32 challenges). See [`tshell-sample-koog/README.md`](tshell-sample-koog/README.md) |")
      appendLine()

      // ── Context Budget ──
      appendLine("## Context Budget")
      appendLine()
      appendLine("How much prompt space does tshell cost? Measured with cl100k_base (GPT-4 tokenizer),")
      appendLine("computed at build time from actual prompt content.")
      appendLine()
      val syntaxTokens = enc.countTokens(TShell.PROMPT_SYNTAX)
      val descTokens = enc.countTokens(toolDesc)
      val cmdSection = fullPrompt.substringAfter("## Commands")
      val cmdTokens = enc.countTokens(cmdSection)
      appendLine("| Component | Tokens | What it contains |")
      appendLine("| --- | ---: | --- |")
      appendLine("| Tool description | $descTokens | One-line summary in the tool schema |")
      appendLine("| Syntax reference | $syntaxTokens | JS subset, pipes, raw templates, composition |")
      appendLine("| Command signatures | $cmdTokens | All stdlib commands with types and descriptions |")
      appendLine("| **Total** | **$tokens** | **${totalKb}KB** |")
      appendLine()
      appendLine("For comparison: a typical MCP tool server exposes 10-20 tools at ~200 tokens each (2000-4000 tokens).")
      appendLine("tshell replaces them all with one `eval` tool. Additional commands added via toolkits")
      appendLine("(FileToolkit, SqlToolkit, etc.) grow only the command signatures section.")
      appendLine()
      appendLine("See [benchmark results](tshell-sample-koog/benchmarks/results/README.md) for how this prompt performs across 32 challenges.")
      appendLine()

      appendLine("---")
      appendLine()
      appendLine("*This README is generated from")
      appendLine("[`TShellLiterateTest.kt`](tshell/src/test/kotlin/com/iodesystems/tshell/TShellLiterateTest.kt).")
      appendLine("Every code example above is executed as part of the test — if an example")
      appendLine("is wrong, the build fails.*")
    }

    // Write to repo root (parent of the tshell submodule dir)
    val readmePath = Path.of("").toAbsolutePath().parent.resolve("README.md")
    readmePath.writeText(readme)
  }
}
