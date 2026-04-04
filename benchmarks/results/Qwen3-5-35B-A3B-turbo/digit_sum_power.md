# digit_sum_power

**Status:** PASS
**Duration:** 6574ms
**Tool calls:** 2

## Prompt

> Using tshell, compute 2 to the power of 15 (use ** operator) and then sum all the digits of the result.

## Final Answer

```
26
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let result = 2 ** 15;
let digits = str(result).split("");
digits |* (d => num(d)) | reduce((sum, d) => sum + d, 0);
```

**Result:**
```
"ERROR: '|' is not supported. Did you mean:\n  |>   pipe        (value |> function)\n  |*   scatter     (array |* function)\n  ||   logical OR  (a || b)\n  |:   bitwise OR  (5 |: 3 → 7)"
```

### Attempt 2 (OK)

```javascript
let result = 2 ** 15;
let digits = str(result).split("");
reduce(digits, (sum, d) => sum + num(d), 0);
```

**Result:**
```
"26"
```

