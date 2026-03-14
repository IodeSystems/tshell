# digit_sum_power

**Status:** PASS
**Duration:** 4543ms
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
let digits = str(power).split('').map(num => num);
let sum = reduce(digits, (acc, d) => acc + num(d), 0);
sum
```

**Result:**
```
"26"
```

