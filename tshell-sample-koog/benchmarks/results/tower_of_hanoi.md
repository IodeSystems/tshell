# tower_of_hanoi

**Status:** PASS
**Duration:** 4346ms
**Tool calls:** 1

## Prompt

> Using tshell, compute the minimum number of moves to solve Tower of Hanoi with 10 disks. Return the number.

## Final Answer

```
1023
```

## Attempts

### Attempt 1 (OK)

```javascript
// Tower of Hanoi minimum moves formula: 2^n - 1
let n = 10;
let result = pow(2, n) - 1;
result
```

**Result:**
```
"1023"
```

