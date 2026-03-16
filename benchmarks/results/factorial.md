# factorial

**Status:** PASS
**Duration:** 21087ms
**Tool calls:** 2

## Prompt

> Compute 7 factorial (7!) using tshell. Return just the number.

## Final Answer

```
5040
```

## Attempts

### Attempt 1 (ERROR)

```javascript
reduce((a, x) => a * x, 1)(range(1, 8))
```

**Result:**
```
"ERROR: Type mismatch in pipe into reduce\n\n  Expected: array\n  Got:      function (function <anonymous>(a, x))\n\n  at line 1:6"
```

### Attempt 2 (OK)

```javascript
range(1, 8) |> reduce((a, x) => a * x, 1)
```

**Result:**
```
"5040"
```

