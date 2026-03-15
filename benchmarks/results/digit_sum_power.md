# digit_sum_power

**Status:** PASS
**Duration:** 4369ms
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
let n = pow(2, 15);
str(n)
  |> split("")
  |> map(num)
  |> reduce((a, x) => a + x, 0)
```

**Result:**
```
"26"
```

