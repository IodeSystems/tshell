# fibonacci_memo

**Status:** PASS
**Duration:** 13533ms
**Tool calls:** 2

## Prompt

> In tshell, implement fibonacci with memoization using an object as cache. Compute fib(20). Return the result.

## Final Answer

```
The result is **6765**.

The implementation uses an object `cache` to memoize Fibonacci results. Since tshell object keys must be strings, I converted the number `n` to a string using `str(n)` when accessing and storing values in the cache. The function recursively computes `fib(20)` with memoization, returning 6765.
```

## Attempts

### Attempt 1 (OK)

```javascript
export let cache = {}
export function fib(n) {
  if (n <= 1) return n
  if (cache[n] != null) return cache[n]
  let result = fib(n - 1) + fib(n - 2)
  cache[n] = result
  return result
}
fib(20)
```

**Result:**
```
"ERROR: Type mismatch in index\n\n  Expected: string\n  Got:      number (20)\n\n  at line 4:11"
```

### Attempt 2 (OK)

```javascript
export let cache = {}
export function fib(n) {
  if (n <= 1) return n
  if (cache[str(n)] != null) return cache[str(n)]
  let result = fib(n - 1) + fib(n - 2)
  cache[str(n)] = result
  return result
}
fib(20)
```

**Result:**
```
"6765"
```

