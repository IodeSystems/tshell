# factorial

**Status:** PASS
**Duration:** 19320ms
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
let fact = (n) => {
  if (n <= 1) return 1;
  return n * fact(n - 1);
};
fact(7)
```

**Result:**
```
"5040"
```

