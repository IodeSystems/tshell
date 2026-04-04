# Plan
<!-- Prioritized roadmap with implementation notes -->

## Done
- [x] Compact prompt LOD (56% token reduction, 33/33 parity)
- [x] `**` exponentiation operator
- [x] `^` / `|` disambiguation with `|:` (bitwise OR) and `|.` (bitwise XOR)
- [x] Braceless loop bodies
- [x] JS compat: `undefined`, `parseInt`, `charCodeAt`, `repeat`, `trimStart`, default params, destructured params, spread destructuring, numeric keys, method shorthand, assignment-as-expression, `let fn()` syntax
- [x] Improved error messages (bare ref detection, `if` without parens, pipe hints)
- [x] Benchmark harness: `--model`, `--fail-fast`, `--compact`, `formatHint`, result dir structure
- [x] `map`/`filter` on objects, `forEach`/`map`/`filter`/`reduce` with index
- [x] `range(n)`, `push`/`pop`, `head`/`first`, `chars`, `countBy()` identity default
- [x] `match()` string patterns: JS non-global semantics with capture groups

## Remaining
- [ ] Better error messages for common regex mistakes
- [ ] Error message for `let f = function(x) { ... }` (function expressions not supported) — actually now supported via `let f(x) { }` syntax
