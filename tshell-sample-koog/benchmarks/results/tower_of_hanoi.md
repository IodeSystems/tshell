# tower_of_hanoi

**Status:** PASS
**Duration:** 5671ms
**Tool calls:** 1

## Prompt

> Using tshell, compute the minimum number of moves to solve Tower of Hanoi with 10 disks. Return the number.

## Final Answer

```
The minimum number of moves to solve the Tower of Hanoi with 10 disks is **1023**.
```

## Attempts

### Attempt 1 (OK)

```javascript
export function hanoi(n) {
  if (n === 0) return 0;
  return 2 * hanoi(n - 1) + 1;
}

let n = 10;
let result = hanoi(n);
result
```

**Result:**
```
"1023"
```

