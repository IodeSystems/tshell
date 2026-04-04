# digit_sum_power

**Status:** PASS
**Duration:** 3788ms
**Tool calls:** 1

## Prompt

> Using tshell, compute 2 to the power of 15 (use ** operator) and then sum all the digits of the result.

## Final Answer

```
26
```

## Attempts

### Attempt 1 (OK)

```javascript
let result = 2 ** 15;
let digits = str(result).split("").map(num).reduce((a, b) => a + b, 0);
digits
```

**Result:**
```
"26"
```

