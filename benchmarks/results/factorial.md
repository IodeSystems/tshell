# factorial

**Status:** PASS
**Duration:** 19911ms
**Tool calls:** 1

## Prompt

> Compute 7 factorial (7!) using tshell. Return just the number.

## Final Answer

```
5040
```

## Attempts

### Attempt 1 (OK)

```javascript
const factorial = (n) => {
  let result = 1;
  for (let i = 2; i <= n; i++) {
    result *= i;
  }
  return result;
};
factorial(7)
```

**Result:**
```
"5040"
```

