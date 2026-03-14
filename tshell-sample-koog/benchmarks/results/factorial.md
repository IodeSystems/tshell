# factorial

**Status:** PASS
**Duration:** 20039ms
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
function factorial(n) {
  if (n <= 1) return 1;
  return n * factorial(n - 1);
}

factorial(7)
```

**Result:**
```
"5040"
```

