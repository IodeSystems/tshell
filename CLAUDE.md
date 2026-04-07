# TShell

Sandboxed scripting language with JS syntax plus pipes, designed to give LLMs safe computation via a single `eval` tool. Type annotations accepted but ignored. Kotlin/JVM, ANTLR4 grammar, Gradle build.

## Project structure

```
tshell/                          # Core language module
  src/main/antlr4/               # ANTLR4 grammar (TShellLexer.g4, TShellParser.g4)
  src/main/java-generated/       # Generated parser (git-ignored, regenerated on build)
  src/main/kotlin/.../runtime/   # Interpreter, Environment, TShellValue, TShellError
  src/main/kotlin/.../toolkit/   # CoreToolkit, MathToolkit, WebToolkit, FileToolkit
  src/test/kotlin/               # Tests (TestNG, ~480 tests)
tshell-graph/                    # Graph database toolkit (nodes, edges, traversal, schema)
tshell-mcp/                      # MCP server + client toolkit
tshell-cli/                      # Standalone MCP server binary
tshell-playwright/               # Browser automation toolkit
tshell-sql/                      # SQL toolkit
tshell-sample-koog/              # LLM integration + benchmark harness (33 challenges)
```

## Key files for language changes

1. `tshell/src/main/antlr4/TShellLexer.g4` — tokens
2. `tshell/src/main/antlr4/TShellParser.g4` — grammar rules
3. `tshell/src/main/kotlin/.../runtime/Interpreter.kt` — eval logic, visitor methods
4. `tshell/src/main/kotlin/.../runtime/Environment.kt` — scope/bindings
5. `tshell/src/main/kotlin/.../runtime/TShellValue.kt` — value types
6. `tshell/src/main/kotlin/.../runtime/TShellError.kt` — error messages
7. `tshell/src/test/kotlin/.../TShellTest.kt` — main test suite
8. `tshell/src/test/kotlin/.../TShellNewFeaturesTest.kt` — new feature tests (~230 tests)
9. `tshell/src/test/kotlin/.../TShellLiterateTest.kt` — generates README from test cases

## Key files for JS compatibility

- `Interpreter.kt` → `JS_METHOD_ALIASES` — maps JS method names to tshell commands (e.g. `toUpperCase` → `upper`)
- `Interpreter.kt` → `JS_METHOD_HINTS` — error messages for unsupported JS methods
- `Interpreter.kt` → `bindMethodOrHint()` — method syntax sugar: `receiver.cmd(args)` → `cmd(receiver, args)`
- `Interpreter.kt` → `bindMutatingArrayMethod()` — `push`/`pop`/`shift`/`unshift`/`splice` with writeback
- `Interpreter.kt` → `GLOBAL_ALIASES` — `Math`, `JSON`, `Object`, `Array`, `console`, `Promise` namespace objects
- `js-compat.md` — full compatibility matrix
- `tshell/src/test/kotlin/.../JsCompatProbeTest.kt` — 45 JS compatibility tests

## Key files for benchmarks

- `tshell-sample-koog/.../localllm/Benchmark.kt` — 33 challenges, scoring, result output
- `tshell-sample-koog/.../localllm/Main.kt` — CLI flags: `--benchmark`, `--compact`, `--model`, `--fail-fast`
- `benchmarks/results/` — full prompt results by model
- `benchmarks/results-compact/` — compact prompt results by model

## Build & test

```bash
# Run all tests (generates parser first)
./gradlew :tshell:test

# Run all module tests
./gradlew test

# Regenerate ANTLR parser after grammar changes
./gradlew generateParser

# Run a single test class
./gradlew :tshell:test --tests "com.iodesystems.tshell.TShellTest"

# Build and run benchmark
./gradlew :tshell-sample-koog:installDist
./tshell-sample-koog/build/install/tshell-sample-koog/bin/tshell-sample-koog \
  --url http://localhost:8111 --model MODEL_NAME --benchmark
# Add --compact for compact prompt mode
```

- Build uses ANTLR4 to generate parser into `src/main/java-generated/`
- Tests use TestNG
- JVM 21, Kotlin
- `TShellLexerBase` is a custom superclass for the lexer (semantic predicates)

## Implementation pattern for language features

1. Add/modify tokens in `TShellLexer.g4` (if needed)
2. Add/modify grammar rules in `TShellParser.g4`
3. Regenerate parser: `./gradlew generateParser`
4. Add visitor method in `Interpreter.kt` — the `eval()` method dispatches on context type
5. Add tests
6. Run tests: `./gradlew :tshell:test`

## Array semantics

- Arrays are mutable references (JS semantics)
- `slice()` returns an independent copy (not a view)
- `push()`/`unshift()` return the array (diverges from JS which returns length — enables chaining)
- `pop()`/`shift()` return the removed element (matches JS)
- `splice()` returns the mutated array (diverges from JS which returns removed elements)
- Method-style calls work: `arr.push(x)`, `arr.sort()`, `str.split("")`

## Conventions

- README.md is auto-generated from `TShellLiterateTest.kt` — do not edit README directly
- Objects and arrays are mutable references (JS semantics); variables are reassignable
- `bindDestructure()` in Interpreter handles all destructuring patterns (simple, object, array)
- `collectDestructureNames()` extracts names for export tracking
- Export wraps letDecl/fnDecl/assignStatement
- Generated parser in `src/main/java-generated/` — do not edit directly

## Docs
- `js-compat.md` — full JS/tshell compatibility matrix
- `plan/todo.md` — session-scoped deferred work and known behaviors
- `plan/plan.md` — prioritized roadmap
