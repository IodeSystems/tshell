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
| `[1,2,3].filter(x => x > 1)` | Direct command |
| `[1,2,3].forEach(fn)` | Iterates, returns null |
| `[1,2].concat([3,4])` | Returns new array |
| `[1,2,3].indexOf(2)` | Returns index or -1 |
| `[1,2,3].some(x => x > 2)` | Boolean |
| `[1,2,3].every(x => x > 0)` | Boolean |
| `[1,2].flatMap(x => [x, x*10])` | Map then flatten |
| `[1,2,3,4].slice(1, 3)` | Extract section |
| `"hello".toUpperCase()` | Alias → `upper()` |
| `"hello".toLowerCase()` | Alias → `lower()` |
| `"hello".includes("ell")` | Alias → `contains()` |
| `"hello".split(",")` | Direct command |
| `"hello".indexOf("ll")` | Returns index or -1 |
| `"hello".slice(1, 4)` | Alias → `substring()` |
| `try { ... } catch(e) { ... }` | Full support, `finally` optional |
| `throw "error"` | Throws TShellError, caught by try/catch |

## Auto-resolved namespaces

LLMs write these constantly. tshell resolves them to the equivalent top-level command.

| JS | tshell equivalent |
|---|---|
| `JSON.parse(s)` | `parseJson(s)` |
| `JSON.stringify(x)` | `toJson(x)` |
| `Math.floor(n)` | `floor(n)` |
| `Math.ceil(n)` | `ceil(n)` |
| `Math.round(n)` | `round(n)` |
| `Math.abs(n)` | `abs(n)` |
| `Math.min(a, b)` | `min(a, b)` |
| `Math.max(a, b)` | `max(a, b)` |
| `Math.pow(a, b)` | `pow(a, b)` |
| `Object.keys(o)` | `keys(o)` |
| `Object.values(o)` | `values(o)` |
| `Object.entries(o)` | `entries(o)` |
| `console.log(x)` | `print(x)` |
| `Array.isArray(x)` | `isArray(x)` |
| `Array.from(x)` | `toArray(x)` |

## Auto-resolved constructors

| JS | tshell equivalent |
|---|---|
| `String(42)` | `str(42)` |
| `Number("42")` | `num("42")` |

## Method aliases (on values)

| JS method | tshell command |
|---|---|
| `.toUpperCase()` | `upper()` |
| `.toLowerCase()` | `lower()` |
| `.includes()` | `contains()` |
| `.replaceAll()` | `replace()` |
| `.matchAll()` | `match()` |
| `.search()` | `test()` |
| `.slice()` (string) | `substring()` |

## Errors with hints

These fail but produce actionable error messages.

| JS pattern | Error suggests |
|---|---|
| `.push(x)` | `[...arr, x]` (immutable) |
| `.pop()`, `.shift()`, `.splice()` | spread to build new arrays |
| `.charAt(i)` | `str[i]` |
| `Boolean(x)` | `x ? true : false` |
| `new Foo()` | no constructors |
| `class Foo {}` | use objects and functions |
| `async function` | `all()`, `race()` |
| `import x from 'y'` | not supported |
| `delete obj.key` | immutable values |
| `Promise.all([])` | `all()` |

## Not supported, no equivalent

| JS feature | Notes |
|---|---|
| `class`, `new`, `this` | Use objects + functions |
| `async/await` | `all()`, `race()`, `any()` for concurrency |
| `import/export` (modules) | `export` persists across evals, no module system |
| `yield`, generators | Not supported |
| `typeof x` (operator) | `typeof(x)` (function) |
| Prototype chain | No prototypes |
| `delete`, `in` operator | Immutable values |
| `void`, `with`, labels | Not supported |
| Bitwise operators | Not supported |
| Regular assignment without `let` | Error — must declare first |
