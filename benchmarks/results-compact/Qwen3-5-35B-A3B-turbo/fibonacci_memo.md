# fibonacci_memo

**Status:** PASS
**Duration:** 5101ms
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
let fib = (n) => {
  if (n <= 1) return n;
  if (cache[n] !== undefined) return cache[n];
  let result = fib(n - 1) + fib(n - 2);
  cache[n] = result;
  return result;
};
fib(20)
```

**Result:**
```
"6765"
```

