# digit_sum_power

**Status:** PASS
**Duration:** 3974ms
**Tool calls:** 1

## Prompt

> Using tshell, compute 2^15 and then sum all the digits of the result. Return the digit sum.

## Final Answer

```
26
```

## Attempts

### Attempt 1 (OK)

```javascript
let result = pow(2, 15);
str(result).split("").map(num).reduce((a, x) => a + x, 0)
```

**Result:**
```
"26"
```

