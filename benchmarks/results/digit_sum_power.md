# digit_sum_power

**Status:** PASS
**Duration:** 21040ms
**Tool calls:** 6

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
let str = str(result);
let digits = str |* (c => num(c));
let sum = digits |* (n => n) | reduce((a, x) => a + x, 0);
sum
```

**Result:**
```
"ERROR: Type mismatch in pipe into reduce\n\n  Expected: array\n  Got:      function (function <anonymous>(a, x))\n\n  at line 4:37"
```

### Attempt 2 (ERROR)

```javascript
let result = pow(2, 15);
let str = str(result);
let digits = str |* (c => num(c));
let sum = digits | reduce((a, x) => a + x, 0);
sum
```

**Result:**
```
"ERROR: Type mismatch in pipe into reduce\n\n  Expected: array\n  Got:      function (function <anonymous>(a, x))\n\n  at line 4:25"
```

### Attempt 3 (OK)

```javascript
let result = pow(2, 15);
let str = str(result);
let digits = str |* (c => num(c));
let sum = reduce(digits, (a, x) => a + x, 0);
sum
```

**Result:**
```
"32768"
```

### Attempt 4 (OK)

```javascript
let result = pow(2, 15);
let str = str(result);
let digits = str |* (c => num(c));
let sum = reduce(digits, (a, x) => a + x, 0);
sum
```

**Result:**
```
"32768"
```

### Attempt 5 (OK)

```javascript
let result = pow(2, 15);
let str = str(result);
let chars = str |* (c => c);
let digits = chars |* (c => num(c));
let sum = reduce(digits, (a, x) => a + x, 0);
sum
```

**Result:**
```
"32768"
```

### Attempt 6 (OK)

```javascript
let result = pow(2, 15);
let str = str(result);
let len = len(str);
let sum = 0;
for (let i = 0; i < len; i++) {
  sum = sum + num(charAt(str, i));
}
sum
```

**Result:**
```
"26"
```

