# fibonacci_memo

**Status:** PASS
**Duration:** 6658ms
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
let fib = function(n: number, cache: object = {}) {
  if (n in cache) return cache[n];
  if (n <= 1) return n;
  let result = fib(n - 1, cache) + fib(n - 2, cache);
  cache[n] = result;
  return result;
};

fib(20, {})
```

**Result:**
```
"6765"
```

