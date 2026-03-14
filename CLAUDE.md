# TShell

Sandboxed scripting language with JS syntax plus pipes, designed to give LLMs safe computation via a single `eval` tool. Type annotations accepted but ignored. Kotlin/JVM, ANTLR4 grammar, Gradle build.

## Project structure

```
tshell/                          # Core language module
  src/main/antlr4/               # ANTLR4 grammar (TShellLexer.g4, TShellParser.g4)
  src/main/java-generated/       # Generated parser (git-ignored, regenerated on build)
  src/main/kotlin/.../runtime/   # Interpreter, Environment, TShellValue, TShellError
  src/main/kotlin/.../toolkit/   # CoreToolkit, FileToolkit, GraphToolkit, etc.
  src/test/kotlin/               # Tests (TestNG)
tshell-playwright/               # Browser automation toolkit
tshell-sql/                      # SQL toolkit
tshell-sample-koog/              # Sample LLM integration
```

## Key files for language changes

1. `tshell/src/main/antlr4/TShellLexer.g4` — tokens
2. `tshell/src/main/antlr4/TShellParser.g4` — grammar rules
3. `tshell/src/main/kotlin/.../runtime/Interpreter.kt` — eval logic, visitor methods
4. `tshell/src/main/kotlin/.../runtime/Environment.kt` — scope/bindings
5. `tshell/src/main/kotlin/.../runtime/TShellValue.kt` — value types
6. `tshell/src/main/kotlin/.../runtime/TShellError.kt` — error messages
7. `tshell/src/test/kotlin/.../TShellTest.kt` — main test suite
8. `tshell/src/test/kotlin/.../TShellNewFeaturesTest.kt` — new feature tests
9. `tshell/src/test/kotlin/.../TShellLiterateTest.kt` — generates README from test cases

## Build & test

```bash
# Run all tests (generates parser first)
./gradlew :tshell:test

# Regenerate ANTLR parser after grammar changes
./gradlew generateParser

# Run a single test class
./gradlew :tshell:test --tests "com.iodesystems.tshell.TShellTest"
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

## Conventions

- README.md is auto-generated from `TShellLiterateTest.kt` — do not edit README directly
- Values are immutable data; variables are reassignable
- `bindDestructure()` in Interpreter handles all destructuring patterns (simple, object, array)
- `collectDestructureNames()` extracts names for export tracking
- Export wraps letDecl/fnDecl/assignStatement

## JS compatibility layer

tshell maximizes JS/TS compatibility so LLM-generated code works without modification. See `js-compat.md` for the full matrix.

**Namespaces** (`JSON`, `Math`, `Object`, `console`, `Array`, `Promise`): registered as TObject bindings in globals via `Interpreter.initBuiltinBindings()`. Command-backed namespaces map JS method names to tshell commands. Skipped if a toolkit already installed a richer version (e.g. MathToolkit). Use `registerNamespace(name, methods, globalFallback)` to add new ones.

**Composition functions** (`all`, `race`, `any`, `chain`): regular functions bound in globals, not grammar keywords. They close over the Interpreter for parallel execution support. `Promise.all`/`Promise.race` resolve through the Promise namespace object.

**Constructor aliases** (`String`, `Number`, `Boolean`, `parseInt`, `parseFloat`): resolved in `visitIdentifierExpr` via `JS_CONSTRUCTOR_ALIASES` map.

**Method aliases** (`.toUpperCase()` → `upper()`, `.includes()` → `contains()`, etc.): resolved in `bindMethodOrHint()` in Interpreter.

**Mutating array methods** (`push`, `pop`, `shift`, `unshift`, `splice`): handled via lvalue tracking in `visitPostfixExpr` + atomic read-modify-write via `Environment.mutate()`.
