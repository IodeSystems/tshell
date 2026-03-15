# tower_of_hanoi

**Status:** PASS
**Duration:** 5285ms
**Tool calls:** 2

## Prompt

> Using tshell, compute the minimum number of moves to solve Tower of Hanoi with 10 disks. Return the number.

## Final Answer

```
1023
```

## Attempts

### Attempt 1 (OK)

```javascript
const n = 10;
2 ** n - 1
```

**Result:**
```
"ERROR: Syntax error at line 2:3\n\n  2 ** n - 1\n     ^\n\n  Expected an expression, got '*'"
```

### Attempt 2 (OK)

```javascript
const n = 10;
Math.pow(2, n) - 1
```

**Result:**
```
"1023"
```

