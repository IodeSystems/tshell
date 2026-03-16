# tower_of_hanoi

**Status:** PASS
**Duration:** 4322ms
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
let n = 10;
// Minimum moves for Tower of Hanoi with n disks is 2^n - 1
let moves = pow(2, n) - 1;
moves
```

**Result:**
```
"1023"
```

