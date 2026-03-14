# fibonacci_memo

**Status:** PASS
**Duration:** 12800ms
**Tool calls:** 3

## Prompt

> In tshell, implement fibonacci with memoization using an object as cache. Compute fib(20). Return the result.

## Final Answer

```
6765
```

## Attempts

### Attempt 1 (OK)

```javascript
export let cache = {}
function fib(n) {
  if (n <= 1) return n
  if (cache[n] !== undefined) return cache[n]
  cache[n] = fib(n-1) + fib(n-2)
  return cache[n]
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
function fib(n) {
  if (n <= 1) return n
  if (cache[str(n)] !== undefined) return cache[str(n)]
  cache[str(n)] = fib(n-1) + fib(n-2)
  return cache[str(n)]
}
fib(20)
```

**Result:**
```
"ERROR: tshell does not have 'undefined' — use null instead\n\n  at line 8:3"
```

### Attempt 3 (OK)

```javascript
export let cache = {}
function fib(n) {
  if (n <= 1) return n
  if (cache[str(n)] != null) return cache[str(n)]
  cache[str(n)] = fib(n-1) + fib(n-2)
  return cache[str(n)]
}
fib(20)
```

**Result:**
```
"6765"
```

