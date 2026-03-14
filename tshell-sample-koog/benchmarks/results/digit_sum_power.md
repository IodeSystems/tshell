# digit_sum_power

**Status:** PASS
**Duration:** 11507ms
**Tool calls:** 2

## Prompt

> Using tshell, compute 2^15 and then sum all the digits of the result. Return the digit sum.

## Final Answer

```
The result is 26.

Here's the breakdown:
- 2^15 = 32768
- Sum of digits: 3 + 2 + 7 + 6 + 8 = 26
```

## Attempts

### Attempt 1 (OK)

```javascript
let pow15 = pow(2, 15);
let str = str(pow15);
let digits = str.map(c => num(c));
let sum = reduce(digits, (acc, n) => acc + n, 0);
sum
```

**Result:**
```
"ERROR: Type mismatch in pipe into map\n\n  Expected: array\n  Got:      string (\"32768\")\n\n  at line 3:20"
```

### Attempt 2 (OK)

```javascript
let pow15 = pow(2, 15);
let str = str(pow15);
let chars = str.split("");
let digits = map(chars, c => num(c));
let sum = reduce(digits, (acc, n) => acc + n, 0);
sum
```

**Result:**
```
"26"
```

