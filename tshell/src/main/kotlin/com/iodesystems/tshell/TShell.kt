package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.*
import com.iodesystems.tshell.toolkit.ToolkitLoader
import java.nio.file.Path

class TShell(
  maxSteps: Int = 1_000_000,
  maxCallDepth: Int = 256,
  timeoutMs: Long = 30_000,
  maxOutputBytes: Int = 64_000,
  resetLimitsOnEval: Boolean = true
) {
  val commands = CommandRegistry()
  val limits = ExecutionLimits(maxSteps, maxCallDepth, timeoutMs, maxOutputBytes, resetLimitsOnEval)
  private val globals = Environment()

  init {
    commands.register(
      name = "help",
      signature = "search?: string",
      description = "list commands; search by name",
      examples = listOf("help()", """help("file")""", """help("graph")""")
    ) { args ->
      val search = (args.firstOrNull() as? TShellValue.TString)?.value
      TShellValue.TString(commands.toHelp(search))
    }
  }

  fun register(
    name: String,
    signature: String,
    description: String,
    examples: List<String> = emptyList(),
    hidden: Boolean = false,
    namespace: String? = null,
    fn: suspend (List<TShellValue>) -> TShellValue
  ): TShell {
    commands.register(name, signature, description, examples, hidden, namespace, fn)
    if (namespace != null) {
      // Auto-lift: rebuild the namespace object in globals
      commands.buildNamespaceObject(namespace)?.let { nsObj ->
        globals.defineOrSet(namespace, nsObj)
      }
    }
    return this
  }

  fun registerGuide(name: String, content: String): TShell {
    commands.registerGuide(name, content)
    return this
  }

  fun eval(source: String): TShellValue {
    if (limits.resetOnEval) limits.reset()
    return Interpreter(commands, globals, limits).eval(source).value
  }

  suspend fun evalAsync(source: String): TShellValue {
    if (limits.resetOnEval) limits.reset()
    return Interpreter(commands, globals, limits).evalAsync(source).value
  }

  /**
   * Evaluates source in an isolated child environment.
   * Only names marked with `export` are promoted to globals (using defineOrSet,
   * so exports can redefine existing globals).
   * On error, nothing is promoted.
   * Designed for LLM agent use where each eval should be self-contained.
   */
  fun evalExported(source: String, vars: Map<String, TShellValue> = emptyMap()): TShellValue {
    if (limits.resetOnEval) limits.reset()
    val childEnv = globals.child()
    for ((name, value) in vars) {
      childEnv.define(name, value)
    }
    val result = Interpreter(commands, childEnv, limits).eval(source)
    for (name in result.exportedNames) {
      val value = childEnv.get(name) ?: continue
      globals.defineOrSet(name, value)
    }
    return result.value
  }

  /**
   * Evaluates multiple sources in a shared isolated child environment.
   * Definitions do not leak into globals. Returns the child environment's bindings.
   * Files can reference definitions from earlier sources in the list.
   */
  fun evalIsolated(sources: List<String>): Map<String, TShellValue> {
    if (limits.resetOnEval) limits.reset()
    val childEnv = globals.child()
    for (source in sources) {
      Interpreter(commands, childEnv, limits).eval(source)
    }
    return childEnv.ownBindings()
  }


  /**
   * Evaluates source with rollback on failure.
   * If evaluation throws, global state is restored to pre-eval snapshot.
   */
  @Deprecated("Use evalTransactional", ReplaceWith("evalTransactional(source)"))
  fun evalSafe(source: String): Result<TShellValue> = evalTransactional(source)

  /**
   * Evaluates source transactionally.
   * If evaluation throws, global state is restored to pre-eval snapshot.
   * Side effects in external stores (graph, files) are NOT rolled back —
   * use GraphStore.fork()/merge() for store-level isolation.
   */
  fun evalTransactional(source: String): Result<TShellValue> {
    if (limits.resetOnEval) limits.reset()
    val snapshot = globals.snapshot()
    return try {
      Result.success(Interpreter(commands, globals, limits).eval(source).value)
    } catch (e: Exception) {
      globals.restore(snapshot)
      Result.failure(e)
    }
  }

  /**
   * Returns all global bindings as a TShellValue.TObject.
   * Functions are included — they serialize as their name/params for inspection.
   */
  fun getState(): TShellValue.TObject {
    return TShellValue.TObject(globals.allBindings())
  }

  /**
   * Injects bindings into the global environment.
   * Useful for seeding state from a prior session or external source.
   */
  fun setState(state: Map<String, TShellValue>) {
    for ((name, value) in state) {
      globals.defineOrSet(name, value)
    }
  }

  /**
   * Injects bindings from a TShellValue.TObject.
   */
  fun setState(state: TShellValue.TObject) {
    setState(state.fields)
  }

  /**
   * Loads all toolkit extensions from subdirectories of [dir].
   * Each subdirectory containing .tshell files becomes a toolkit.
   *
   * @param namespaces map of toolkit directory name to namespace alias.
   *   e.g. mapOf("graph" to "g") → g.addNode(...) instead of addNode(...)
   */
  fun loadToolkits(dir: Path, namespaces: Map<String, String> = emptyMap()): List<ToolkitLoader.LoadResult> {
    return ToolkitLoader(dir).loadAll(this, namespaces)
  }

  /**
   * Loads a single toolkit extension from [toolkitDir].
   *
   * @param namespace if non-null, functions are bound to this name as an object.
   *   e.g. namespace="g" → g.myFn(...)
   */
  fun loadToolkit(toolkitDir: Path, namespace: String? = null): ToolkitLoader.LoadResult {
    return ToolkitLoader(toolkitDir.parent ?: toolkitDir).loadToolkit(this, toolkitDir, namespace)
  }

  /**
   * Removes bindings from the global environment.
   */
  fun removeGlobals(names: Set<String>) {
    globals.remove(names)
  }

  /**
   * Generates a system prompt describing tshell syntax and available commands.
   *
   * @param compact if true, lists command names only (no signatures) and points
   *   to help() for details. Reduces prompt weight by ~50% while relying on
   *   runtime discovery (help, guides, error messages) for specifics.
   *   Default false preserves the full listing for maximum first-try accuracy.
   */
  fun toPrompt(compact: Boolean = false): String = buildString {
    appendLine("# tshell — language reference")
    appendLine("JS subset with pipes. Type annotations are accepted but ignored. Only commands listed below exist.")
    appendLine()
    appendLine(PROMPT_SYNTAX)
    appendLine()
    if (compact) {
      appendLine("## Commands")
      appendLine(commands.toCompactPrompt())
    } else {
      appendLine("## Commands (first arg is pipe input unless noted)")
      appendLine(commands.toPrompt())
    }
  }

  companion object {
    /**
     * Syntax reference used in toPrompt(). Kept as a const so tests can verify
     * that every example line is valid tshell.
     */
    const val PROMPT_SYNTAX = """## Syntax: JS subset
let/const/var, function, =>, if/else, while, for/for..of/for..in, switch/case,
try/catch/finally, throw, break, continue, destructuring, spread, ?., ??, ternary,
template strings, regex, typeof, ===, bitwise. All work as expected.
Not supported: class, new, this, import, yield, async/await, generators.
Last expression is the output. `let` returns null — end with the value you want.

## Key differences from JS
export let x = 10            // persists across eval calls; without export, discarded
fn(name: "Alice", age: 30)  // named args
[1,2,3] |> map(n => n * 10) // pipe: passes left as first arg
[1,2,3] |* (x => x * 2) |> reduce((a, x) => a + x, 0)  // scatter then pipe
"hello".toUpperCase()        // JS methods auto-resolve to commands

## Raw template strings — use for edit(), write(), replace()
// r`...` — backslashes literal, ${'$'}{expr} interpolation works
r`C:\Users\${'$'}{name}`              // → C:\Users\Alice
edit("f.ini", r`C:\OldApp`, r`C:\NewApp`)

## Composition
all(() => a(), () => b()) // parallel   race() // first success
chain(() => getData(), d => transform(d)) // sequential"""

    /**
     * Executable examples from the prompt syntax section.
     * Tests verify these parse and run without error.
     */
    /**
     * Short tool description for LLM tool-call schemas.
     * Kept here so it stays in sync with the language and can be verified by tests.
     */
    const val TOOL_DESCRIPTION =
      "Execute tshell code (JS subset with pipes). " +
      "Use r`...` for strings with backslashes — they stay literal. " +
      "Pass data via vars to avoid double-escaping: {\"code\": \"process(input)\", \"vars\": {\"input\": \"value\"}}. " +
      "export persists values across calls; without export, state is discarded. " +
      "help() lists commands. If code errors, read the message, fix, retry."

    const val PROMPT_EXAMPLES = """let x = 42
let a, b = 1, c = "two"
x = 99
x += 1
function fib(n) { if (n <= 1) { return n } else { return fib(n-1) + fib(n-2) } }
let double = x => x * 2
let add = (a, b) => a + b
if (x > 5) { "big" } else { "small" }
let ternary = true ? "yes" : "no"
export let shared = 42
export function double(n) { return n * 2 }
let i = 0
while (i < 5) { i += 1 }
do { i += 1 } while (i < 10)
for (let x of [1,2,3]) { x }
for (let i = 0; i < 3; i++) { i }
let v = "b"
switch (v) { case "a": "first"; break; case "b": "second"; break; default: "other" }
[1,2,3] |> filter(n => n > 1) |> map(n => n * 10)
[3,1,2] |> sort
[3,1,2] |> sort("name")
[1,2,3] |> len
let {name, age} = {name: "alice", age: 30}
3 |> add <| 4
[10, 20, 30] |> [a, b, c]
add(b: 2, a: 1)
[1,2,3].map(x => x * 2)
"hello".toUpperCase()
all(() => 1, () => 2)
any(() => null, () => 42)
try { fail("oops") } catch(e) { "caught" }
try { throw "oops" } catch(e) { e }"""
  }
}
