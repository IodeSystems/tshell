# factorial

**Status:** PASS
**Duration:** 6816ms
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
let n = 7;
let result = 1;
for (let i = 1; i <= n; i++) {
    result *= i;
}
result
```

**Result:**
```
"5040"
```

