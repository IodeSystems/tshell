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
  fun evalExported(source: String): TShellValue {
    if (limits.resetOnEval) limits.reset()
    val childEnv = globals.child()
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
    appendLine("Not JS/TS. Similar tokens, different grammar. Only commands listed below exist.")
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
    const val PROMPT_SYNTAX = """## Variables
let x = 42              // declare (required — no bare assignment); var/const also accepted
let a, b = 1, c = "two" // multi-bind: uninitialized → null
x = 99; x += 1          // reassign, compound: +=, -=, *=, /=, %=
export let y = 10       // export persists across eval calls; without export, discarded
export function add(a, b) { return a + b }

## Functions
function fib(n) { if (n <= 1) { return n } else { return fib(n-1) + fib(n-2) } }
let double = x => x * 2            // arrow lambda
let add = (a, b) => a + b          // NO 'fn' keyword — use function or let

## Control flow — same as JS
if/else, while, do...while, for(;;), for..of, switch/case/default, break, continue
// No: try/catch (use fail()), yield, async/await (use all()/race())

## Pipes
[1,2,3] |> filter(n => n > 1) |> map(n => n * 10)  // |> passes left as first arg
[3,1,2] |> sort("name")       // sort([3,1,2], "name") — extra args in parens
3 |> add <| 4                 // <| adds right-side args: add(3, 4)
[10, 20, 30] |> [a, b, c]    // destructure: a=10, b=20, c=30
// |* scatter: normalize to array (null→[], non-array→[x]), parallel map each element
[1,2,3] |* (x => x * 2)     // → [2, 4, 6] (parallel)
[1,2,3] |* double |> reduce((acc, x) => acc + x)  // scatter then reduce

## Method syntax
[1,2,3].map(x => x * 2)     // resolves to map([1,2,3], x => x * 2)
"hello".toUpperCase()        // resolves to upper("hello")
[1,2,3].includes(2)         // resolves to contains([1,2,3], 2)

## Composition
all(() => getA(), () => getB())    // parallel execution, returns [resultA, resultB]
race(() => tryA(), () => tryB())   // parallel, returns first success (cancels rest)
any(() => tryA(), () => tryB())    // sequential, returns first truthy result
chain(() => getData(), d => transform(d))  // sequential pipeline

## Literals and operators
"str", 'str', `template ${'$'}{x}`              // strings
/pattern/flags                               // regex: /[0-9]+/gi, used with match, replace, split, test
[1, 2, 3], {key: val}                        // arrays, objects
obj.field, arr[0], obj?.field                 // access, optional chaining
==, !=, <, >, <=, >=, &&, ||, !, a ?? b      // comparison, logic, null coalescing
[...a, ...b], {...a, key: val}               // spread
let {name, age} = obj; let [a, b] = arr      // destructuring
fn(name: "Alice", age: 30)                   // named args (matched to param names)

## Output
The last expression's value is printed as the program result. `let` returns null.
let x = [1, 2, 3]; x |> map(n => n * 10)  // output: [10, 20, 30]

## Not supported: class, new, this, import, try/catch, yield, async/await"""

    /**
     * Executable examples from the prompt syntax section.
     * Tests verify these parse and run without error.
     */
    /**
     * Short tool description for LLM tool-call schemas.
     * Kept here so it stays in sync with the language and can be verified by tests.
     */
    const val TOOL_DESCRIPTION =
      "Execute tshell code. NOT JavaScript or Kotlin. " +
      "The last expression's value is the output (let returns null — end with the value you want). " +
      "Variables: let x = 5. Named functions: function name(x) { return x * 2 }. " +
      "Lambdas: let f = x => x + 1. Pipes: [1,2,3] |> map(n => n * 10). " +
      "Regex: /pattern/flags with match, replace, split, test. " +
      "Method syntax: arr.map(fn), str.toUpperCase() — auto-resolves to commands. " +
      "Parallel: all(() => a(), () => b()), race(), |* scatter. " +
      "Use 'export' to persist values across calls: export let x = 5, export function f(n) { ... }. " +
      "Without export, all state is discarded after this call. " +
      "Use help() to discover commands, help(\"name\") for signatures. " +
      "Output is limited — use limit(), filter(), or read(path, start, lines) to reduce large results."

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
any(() => null, () => 42)"""
  }
}
