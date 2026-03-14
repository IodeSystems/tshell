# JS → tshell compatibility

tshell accepts most JS/TS syntax LLMs produce. This doc tracks what works, what auto-resolves, and what doesn't.

## Works as-is

| JS | Notes |
|---|---|
| `let x = 42` | `const`, `var` also accepted |
| `let a, b = 1, c = "two"` | Multi-binding, uninitialized → null |
| `function f(x) { return x }` | Named functions |
| `(x) => x * 2` | Arrow functions |
| `if/else`, `while`, `for`, `for..of`, `switch/case` | Control flow |
| `===` / `!==` | Treated as `==` / `!=` |
| Template strings, destructuring, spread | Full support |
| `?.` optional chaining, `??` null coalescing | Full support |
| `.length` on arrays and strings | Property access |
| `[1,2,3].map(x => x * 2)` | Method syntax → command resolution |
| `"hello".toUpperCase()` | Alias → `upper()` |
| `"hello".includes("ell")` | Alias → `contains()` |
| `"hello".toLowerCase()` | Alias → `lower()` |
| `"hello".split(",")` | Direct command |
| `[1,2,3].filter(x => x > 1)` | Direct command |

## Auto-resolved namespaces

LLMs write these constantly. tshell resolves them to the equivalent top-level command.

| JS | tshell equivalent | Status |
|---|---|---|
| `JSON.parse(s)` | `parseJson(s)` | **auto-resolved** |
| `JSON.stringify(x)` | `toJson(x)` | **auto-resolved** |
| `Math.floor(n)` | `floor(n)` | **auto-resolved** |
| `Math.ceil(n)` | `ceil(n)` | **auto-resolved** |
| `Math.round(n)` | `round(n)` | **auto-resolved** |
| `Math.abs(n)` | `abs(n)` | **auto-resolved** |
| `Math.min(a, b)` | `min(a, b)` | **auto-resolved** |
| `Math.max(a, b)` | `max(a, b)` | **auto-resolved** |
| `Math.pow(a, b)` | `pow(a, b)` | **auto-resolved** |
| `Object.keys(o)` | `keys(o)` | **auto-resolved** |
| `Object.values(o)` | `values(o)` | **auto-resolved** |
| `Object.entries(o)` | `entries(o)` | **auto-resolved** |
| `console.log(x)` | `print(x)` | **auto-resolved** |

## Method aliases (on values)

| JS method | tshell command |
|---|---|
| `.toUpperCase()` | `upper()` |
| `.toLowerCase()` | `lower()` |
| `.includes()` | `contains()` |
| `.replaceAll()` | `replace()` |
| `.matchAll()` | `match()` |
| `.search()` | `test()` |

## Errors with hints

These fail but produce actionable error messages pointing to the tshell equivalent.

| JS pattern | Error suggests |
|---|---|
| `.forEach(fn)` | `map(fn)` or `for..of` |
| `.push(x)` | `[...arr, x]` (immutable) |
| `.pop()`, `.shift()`, `.splice()` | spread to build new arrays |
| `.concat(b)` | `[...a, ...b]` |
| `.every(fn)` | `filter(fn) + len()` |
| `.some(fn)` | `find(fn) != null` |
| `.indexOf(x)` | `find(x => x == value)` |
| `.flatMap(fn)` | `map(fn) \|> flat()` |
| `.charAt(i)` | `str[i]` |
| `String(42)` | `str(42)` |
| `Number("42")` | `num("42")` |
| `new Foo()` | no constructors |
| `class Foo {}` | use objects and functions |
| `throw "err"` | `fail("err")` |
| `async function` | `all()`, `race()` |
| `try/catch` | not supported, use `fail()` |
| `import x from 'y'` | not supported |
| `delete obj.key` | immutable values |
| `Promise.all([])` | `all()` |

## Not supported, no equivalent

| JS feature | Notes |
|---|---|
| `class`, `new`, `this` | Use objects + functions |
| `try/catch/throw` | `fail()` for errors |
| `async/await` | `all()`, `race()`, `any()` for concurrency |
| `import/export` (modules) | `export` persists across evals, no module system |
| `yield`, generators | Not supported |
| `typeof x` (operator) | `typeof(x)` (function) |
| Prototype chain | No prototypes |
| `delete`, `in` operator | Immutable values |
| `void`, `with`, labels | Not supported |
| Bitwise operators | Not supported |
| Regular assignment without `let` | Error — must declare first |
