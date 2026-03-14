# fibonacci_memo

**Status:** PASS
**Duration:** 16121ms
**Tool calls:** 3

## Prompt

> In tshell, implement fibonacci with memoization using an object as cache. Compute fib(20). Return the result.

## Final Answer

```
The result of `fib(20)` is **6765**.

The implementation uses an object `cache` to store previously computed Fibonacci values, with keys converted to strings (since object keys must be strings in tshell). Each time `fib(n)` is called, it first checks if the result is already in the cache. If not, it computes the value recursively and stores it before returning.
```

## Attempts

### Attempt 1 (OK)

```javascript
let cache = {}
function fib(n) {
  if (n <= 1) return n
  if (cache[n] !== undefined) return cache[n]
  cache[n] = fib(n - 1) + fib(n - 2)
  return cache[n]
}
export fib = fib
fib(20)
```

**Result:**
```
"ERROR: Type mismatch in index\n\n  Expected: string\n  Got:      number (20)\n\n  at line 4:11"
```

### Attempt 2 (OK)

```javascript
let cache = {}
function fib(n) {
  if (n <= 1) return n
  let key = str(n)
  if (cache[key] !== undefined) return cache[key]
  cache[key] = fib(n - 1) + fib(n - 2)
  return cache[key]
}
fib(20)
```

**Result:**
```
"ERROR: tshell does not have 'undefined' — use null instead\n\n  at line 9:3"
```

### Attempt 3 (OK)

```javascript
let cache = {}
function fib(n) {
  if (n <= 1) return n
  let key = str(n)
  if (cache[key] !== null) return cache[key]
  cache[key] = fib(n - 1) + fib(n - 2)
  return cache[key]
}
fib(20)
```

**Result:**
```
"6765"
```

