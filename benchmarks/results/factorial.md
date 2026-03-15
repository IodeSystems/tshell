# factorial

**Status:** PASS
**Duration:** 19193ms
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
let result = 1;
for (let i = 1; i <= 7; i++) {
    result = result * i;
}
result
```

**Result:**
```
"5040"
```

