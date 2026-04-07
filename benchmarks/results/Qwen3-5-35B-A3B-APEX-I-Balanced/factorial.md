# factorial

**Status:** PASS
**Duration:** 10586ms
**Tool calls:** 1

## Prompt

> Compute 7 factorial (7!) using tshell.

## Final Answer

```
5040
```

## Attempts

### Attempt 1 (OK)

```javascript
let fact = n => n <= 1 ? 1 : n * fact(n - 1);
fact(7)
```

**Result:**
```
"5040"
```

