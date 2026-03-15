# digit_sum_power

**Status:** PASS
**Duration:** 4657ms
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
let digits = str(result)
  .split('')
  .map(d => num(d));
let sum = digits.reduce((a, x) => a + x, 0);
sum
```

**Result:**
```
"26"
```

