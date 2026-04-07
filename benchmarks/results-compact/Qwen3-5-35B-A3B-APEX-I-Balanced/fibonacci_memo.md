# fibonacci_memo

**Status:** PASS
**Duration:** 6602ms
**Tool calls:** 1

## Prompt

> In tshell, implement fibonacci (fib(0)=0, fib(1)=1) with memoization using an object as cache. Compute fib(20).

## Final Answer

```
6765
```

## Attempts

### Attempt 1 (OK)

```javascript
let cache = {};
function fib(n) {
  if (n in cache) return cache[n];
  if (n < 2) return n;
  cache[n] = fib(n-1) + fib(n-2);
  return cache[n];
}
fib(20)
```

**Result:**
```
"6765"
```

