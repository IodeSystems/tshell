# factorial

**Status:** PASS
**Duration:** 21835ms
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
[1,2,3,4,5,6,7] | reduce((a, x) => a * x, 1)
```

**Result:**
```
"ERROR: Type mismatch in pipe into reduce\n\n  Expected: array\n  Got:      function (function <anonymous>(a, x))\n\n  at line 1:24"
```

### Attempt 2 (OK)

```javascript
let arr = [1,2,3,4,5,6,7];
let result = arr.reduce((a, x) => a * x, 1);
result
```

**Result:**
```
"5040"
```

