# JS → tshell compatibility

tshell accepts most JS syntax LLMs produce. Type annotations are accepted but ignored. This doc tracks what works, what auto-resolves, and what doesn't.

## Works as-is

| JS | Notes |
|---|---|
| `let x = 42` | `const`, `var` also accepted |
| `let a, b = 1, c = "two"` | Multi-binding, uninitialized → null |
| `x = 5` | Reassignment (var must be declared first) |
| `function f(x) { return x }` | Named functions |
| `(x) => x * 2` | Arrow functions |
| `if/else`, `while`, `for`, `for..of`, `for..in`, `switch/case` | Control flow |
| `===` / `!==` | Treated as `==` / `!=` |
| `typeof x` | Operator and function form both work |
| Template strings, destructuring, spread | Full support |
| `?.` optional chaining, `??` null coalescing | Full support |
| `try { ... } catch(e) { ... }` | Full support, `finally` optional |
| `throw "error"` | Throws TShellError, caught by try/catch |
| `"a" in obj` | Key membership (objects and arrays) |
| `.length` on arrays and strings | Property access |
| Method syntax: `arr.map(fn)`, `str.trim()` | Auto-resolves to command call |
| Bitwise `&`, `\|`, `^`, `~`, `<<`, `>>`, `>>>` | Full support, including compound assigns |

### Array methods (all work as method syntax)

| JS | Notes |
|---|---|
| `.map(fn)`, `.filter(fn)`, `.find(fn)` | Direct commands |
| `.reduce(fn, init)` | Direct command |
| `.forEach(fn)` | Iterates, returns null |
| `.some(fn)`, `.every(fn)` | Boolean |
| `.indexOf(v)` | Returns index or -1 |
| `.includes(v)` | Alias → `contains()` |
| `.concat(arr)` | Returns new array |
| `.flatMap(fn)` | Map then flatten |
| `.slice(start, end)` | Extract section |
| `.join(sep)` | Direct command |
| `.sort()`, `.reverse()` | Direct commands |
| `.push(x)`, `.pop()`, `.shift()`, `.unshift(x)` | Mutates variable |
| `.splice(i, n, ...items)` | Mutates variable |
| `.at(-1)` | Negative indexing support |

### String methods (all work as method syntax)

| JS | Notes |
|---|---|
| `.toUpperCase()`, `.toLowerCase()` | Alias → `upper()` / `lower()` |
| `.includes(s)` | Alias → `contains()` |
| `.indexOf(s)` | Direct command |
| `.split(sep)` | Direct command |
| `.trim()` | Direct command |
| `.startsWith(s)`, `.endsWith(s)` | Direct commands |
| `.replace(old, new)` | Replaces all (not just first) |
| `.replaceAll(old, new)` | Alias → `replace()` |
| `.slice(start, end)` | Alias → `substring()` |
| `.charAt(i)` | Direct command |
| `.padStart(n)`, `.padEnd(n)` | Direct commands |
| `.matchAll(re)` | Alias → `match()` |
| `.search(re)` | Alias → `test()` |
| `.at(-1)` | Negative indexing support |

## Namespace objects

Real objects in global scope. Methods also work as top-level commands.

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
| `Promise.all(fns)` | `all(fns)` |
| `Promise.race(fns)` | `race(fns)` |

## Constructor aliases

Global bindings that resolve to tshell commands.

| JS | tshell equivalent |
|---|---|
| `String(42)` | `str(42)` |
| `Number("42")` | `num("42")` |
| `Boolean(x)` | `bool(x)` |
| `parseInt("42")` | `num("42")` |
| `parseFloat("3.14")` | `num("3.14")` |

## Errors with hints

These fail but produce actionable error messages.

| JS pattern | Error suggests |
|---|---|
| `new Foo()` | no constructors |
| `class Foo {}` | use objects and functions |
| `async function` | `all()`, `race()` |
| `import x from 'y'` | not supported |
| `delete obj.key` | immutable values |

## Deliberately excluded

| JS feature | Reason |
|---|---|
| `class`, `new`, `this`, prototypes | Use objects + functions |
| `async/await` | `all()`, `race()`, `any()` for concurrency |
| `import/export` (modules) | `export` persists across evals, no module system |
| `yield`, generators | Not supported |
| `delete` | Immutable values — build new object without the key |
| `void`, `with`, labels | Not needed |
