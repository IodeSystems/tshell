# fibonacci_memo

**Status:** PASS
**Duration:** 13377ms
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
let cache = {}
let fib = n => {
  if (n <= 1) return n
  if (cache[n] !== undefined) return cache[n]
  cache[n] = fib(n - 1) + fib(n - 2)
  return cache[n]
}
export let result = fib(20)
result
```

**Result:**
```
"ERROR: Type mismatch in index\n\n  Expected: string\n  Got:      number (20)\n\n  at line 4:11"
```

### Attempt 2 (OK)

```javascript
let cache = {}
let fib = n => {
  if (n <= 1) return n
  let key = str(n)
  if (cache[key] !== undefined) return cache[key]
  cache[key] = fib(n - 1) + fib(n - 2)
  return cache[key]
}
export let result = fib(20)
result
```

**Result:**
```
"ERROR: tshell does not have 'undefined' — use null instead\n\n  at line 9:23"
```

### Attempt 3 (OK)

```javascript
let cache = {}
let fib = n => {
  if (n <= 1) return n
  let key = str(n)
  if (cache[key] != null) return cache[key]
  cache[key] = fib(n - 1) + fib(n - 2)
  return cache[key]
}
export let result = fib(20)
result
```

**Result:**
```
"6765"
```

