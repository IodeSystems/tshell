# factorial

**Status:** PASS
**Duration:** 20120ms
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
let factorial = n => n <= 1 ? 1 : n * factorial(n - 1);
factorial(7)
```

**Result:**
```
"5040"
```

