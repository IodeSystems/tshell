# fibonacci_memo

**Status:** PASS
**Duration:** 9011ms
**Tool calls:** 2

## Prompt

> In tshell, implement fibonacci with memoization using an object as cache. Compute fib(20). Return the result.

## Final Answer

```
6765
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let cache = {};
let fib = n => {
  if (n <= 1) return n;
  if (cache[n] !== undefined) return cache[n];
  cache[n] = fib(n - 1) + fib(n - 2);
  return cache[n];
};
fib(20);
```

**Result:**
```
"ERROR: tshell does not have 'undefined' — use null instead\n\n  at line 8:3"
```

### Attempt 2 (OK)

```javascript
let cache = {};
let fib = n => {
  if (n <= 1) return n;
  if (cache[n] != null) return cache[n];
  cache[n] = fib(n - 1) + fib(n - 2);
  return cache[n];
};
fib(20);
```

**Result:**
```
"6765"
```

