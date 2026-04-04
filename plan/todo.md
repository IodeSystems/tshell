# TODO

## Fixed (this session)

### Compact prompt LOD
MCP `prompt` tool defaults to compact mode (720 tokens vs 1641 full). LLMs call
`help("name")` for details on demand. 56% token reduction at parity on 33/33 benchmark.

### Exponentiation operator `**`
`2 ** 10` → 1024. Right-associative. `**=` compound assign. Added to syntax reference.

### `^` and `|` disambiguation
Both error immediately with actionable suggestions:
- `^` → suggests `**` (exponentiation) or `|.` (bitwise XOR)
- `|` → suggests `|>` (pipe), `|*` (scatter), `||` (logical OR), `|:` (bitwise OR)
- `| 0` truncation idiom → additionally suggests `floor(x)`

### Bitwise operators `|:` and `|.`
`5 |: 3` → 7 (bitwise OR), `5 |. 3` → 6 (bitwise XOR). Unambiguous pipe-prefixed syntax.

### `number.toString()`, `boolean.toString()`, `number.toFixed(n)`
Direct method calls on primitives.

### Braceless loop bodies
`for (let i = 0; i < n; i++) sum += i;` — all loops support single-statement bodies.

### Comma expressions
`(a, b, c)` evaluates all, returns last.

### Assignment as expression
`x => obj.a = x` works. `(acc[key] = []).push(item)` works.

### Default parameter values
`function fib(n, cache = {})` — defaults evaluated at call time when arg missing.

### Destructured params
`([a, b]) => a + b` — array/object destructuring in arrow and function params.

### Spread in array destructuring
`let [first, ...rest] = arr` — rest element collects remaining items.

### Numeric object keys
`{0: "a", 1: "b"}` — numbers as object field names.

### Shorthand method syntax
`{on(event, handler) { ... }}` — ES6 method shorthand in object literals.

### `let fn()` and `let function fn()` syntax
Both accepted as function declarations alongside `function fn()`.

### Type annotations on let
`let x: number = 5` — parsed and ignored (documentation only).

### `undefined` keyword
`undefined` is an alias for `null`. `if (x !== undefined)` works.

### JS compat functions (hidden)
`parseInt`, `parseFloat`, `Number`, `String`, `Boolean` — all work as expected.

### String methods
`charCodeAt`, `codePointAt`, `repeat`, `trimStart`/`trimLeft`, `trimEnd`/`trimRight` —
all work as both `str.method()` and `method(str)`.

### Array methods
`.entries()` → `[[0, el], [1, el], ...]`, `.len()` via method resolution.

### `lastIndexOf` command
`"foo bar foo" |> lastIndexOf("foo")` → 8.

### `map`/`filter` on objects
`{a: 1, b: 2} |> map(v => v * 10)` → `{a: 10, b: 20}`.
Callback receives `(value, key)`. Objects auto-convert to entries when array expected.

### `forEach`/`map`/`filter`/`reduce` with index
All array iteration callbacks receive index as extra arg: `(element, index)` or `(acc, element, index)`.

### `range(n)` single-arg
`range(5)` → `[0, 1, 2, 3, 4]`.

### `push`/`pop` as commands
`push(arr, val)` and `arr.push(val)` both work.

### `head`/`first` commands
Hidden aliases — `arr |> head()` returns first element.

### `chars` command
`"hello" |> chars()` → `["h", "e", "l", "l", "o"]`.

### `countBy` default identity
`["a", "b", "a"] |> countBy()` → `{a: 2, b: 1}`.

### `match()` with string patterns
Now returns `[fullMatch, group1, ...]` (JS non-global semantics) instead of all matches.

### Bare ref detection
`mid;` inside a block (non-terminal) errors: "has no effect — did you mean: return mid".

### Improved error messages
- `if condition {` without parens → "if requires parentheses"
- `.len` in comparison → "Did you mean .length or len(x)?"
- `requireArray` on object → auto-converts to entries
- `requireArray` hints: suggests `|>` and `|*`
- Duplicate "Hint: Hint:" prefix fixed

### Benchmark improvements
- `--model` flag for explicit model selection
- `--fail-fast` flag to stop on first failure
- `--compact` flag for compact prompt mode
- `formatHint` field on teasers (steers tool usage)
- Results dir restructured: `benchmarks/results/{model}/` and `benchmarks/results-compact/{model}/`

## Not bugs (documented behavior)

### `split` with alternation produces empty strings
`"3+4*2-1".split(/\+|\-|\*|\/|/)` produces empties due to the trailing empty
alternation branch. This matches JS behavior. LLMs need to filter empties.

### `Math.random()` sort shuffle
Not a bug — `sort` expects a comparator, not a random number generator. Use `shuffle`.
