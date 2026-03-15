# tower_of_hanoi

**Status:** PASS
**Duration:** 5964ms
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
const hanoi = (n) => 2 ** n - 1;
hanoi(10)
```

**Result:**
```
"ERROR: Syntax error at line 1:24\n\n  const hanoi = (n) => 2 ** n - 1;\n                          ^\n\n  Unexpected syntax at '*'\n\n  This doesn't look like a valid statement or expression"
```

### Attempt 2 (OK)

```javascript
const hanoi = (n) => Math.pow(2, n) - 1;
hanoi(10)
```

**Result:**
```
"1023"
```

