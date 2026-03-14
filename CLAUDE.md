# TShell

Sandboxed scripting language with TypeScript-like syntax, designed to give LLMs safe computation via a single `eval` tool. Kotlin/JVM, ANTLR4 grammar, Gradle build.

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
