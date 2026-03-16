# digit_sum_power

**Status:** PASS
**Duration:** 9345ms
**Tool calls:** 3

## Prompt

> Using tshell, compute 2^15 and then sum all the digits of the result. Return the digit sum.

## Final Answer

```
26
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let result = pow(2, 15);
str(result) | split("") | map(num) | reduce((a, x) => a + x, 0)
```

**Result:**
```
"ERROR: Type mismatch in '|'\n\n  Expected: number\n  Got:      string (\"32768\")"
```

### Attempt 2 (ERROR)

```javascript
let result = pow(2, 15);
str(result) | split("") | map(n => num(n)) | reduce((a, x) => a + x, 0)
```

**Result:**
```
"ERROR: Type mismatch in '|'\n\n  Expected: number\n  Got:      string (\"32768\")"
```

### Attempt 3 (OK)

```javascript
let result = pow(2, 15);
str(result) |> split("") |> map(n => num(n)) |> reduce((a, x) => a + x, 0)
```

**Result:**
```
"26"
```

