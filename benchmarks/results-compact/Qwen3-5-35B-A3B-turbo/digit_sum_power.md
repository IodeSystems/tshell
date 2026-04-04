# digit_sum_power

**Status:** PASS
**Duration:** 3928ms
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
let str = str(result);
let sum = 0;
for (let c of str) {
  sum += num(c);
}
sum
```

**Result:**
```
"26"
```

