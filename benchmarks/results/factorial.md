# factorial

**Status:** PASS
**Duration:** 19577ms
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
[1,2,3,4,5,6,7] |> reduce((a, x) => a * x, 1)
```

**Result:**
```
"5040"
```

