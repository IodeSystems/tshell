# TODO

## Fixed (this session)

### Regex character classes
`/[+\-*/]/` now lexes correctly. The ANTLR regex rule was treating `/` inside `[...]`
as the closing delimiter. Fixed by adding a `[...]` branch to the lexer rule that
consumes character class contents (including unescaped `/`) before checking for the
closing delimiter.

### Dynamic field + method call (reduce_groupby pattern)
`acc[key] = []; acc[key].push("apple")` now works. The lvalue tracking in
`visitPostfixExpr` was clearing the path on all bracket access. Fixed to preserve
the path when the target is a `TObject` and the index is a `TString` — this allows
the mutating array method writeback to find the correct variable to update.

### Added `shuffle` command
`[1,2,3] |> shuffle()` — randomly reorders array elements. LLMs kept trying to
implement shuffle via `sort(() => Math.random())` which doesn't work.

## Not bugs (documented behavior)

### `split` with alternation produces empty strings
`"3+4*2-1".split(/\+|\-|\*|\/|/)` produces empties due to the trailing empty
alternation branch. This matches JS behavior. LLMs need to filter empties.

### `i++` in while loops
Works correctly as a statement. LLMs sometimes write broken loop logic around it
but `i++` itself is not the problem.

### `Math.random()` sort shuffle
Not a bug — `sort` expects a comparator, not a random number generator. Now moot
since `shuffle` is available.

## Remaining improvements
- [ ] Better error messages for common regex mistakes
- [ ] Error message improvement when LLM tries `let f = function(x) { ... }` (function expressions not supported — use arrow or named function)
