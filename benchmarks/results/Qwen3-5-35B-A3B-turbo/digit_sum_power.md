# digit_sum_power

**Status:** PASS
**Duration:** 4137ms
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
result.toString()
  .split('')
  .map(d => parseInt(d))
  .reduce((a, b) => a + b, 0)
```

**Result:**
```
"26"
```

