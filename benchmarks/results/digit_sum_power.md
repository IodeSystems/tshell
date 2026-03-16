# digit_sum_power

**Status:** PASS
**Duration:** 4686ms
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
let power = pow(2, 15);
let str = str(power);
let digits = str.split("").map(c => num(c));
let sum = digits |> reduce((a, x) => a + x, 0);
sum
```

**Result:**
```
"26"
```

