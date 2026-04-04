# bitwise_flags

**Status:** PASS
**Duration:** 5460ms
**Tool calls:** 1

## Prompt

> In tshell, define permission flags: READ=4, WRITE=2, EXEC=1. Combine READ+WRITE into a variable (addition for non-overlapping flags). Check if it has WRITE (using & and !== 0), check if it has EXEC.

## Final Answer

```
{hasWrite: true, hasExec: false}
```

## Attempts

### Attempt 1 (OK)

```javascript
const READ = 4, WRITE = 2, EXEC = 1;
const READ_WRITE = READ + WRITE;
const hasWrite = (READ_WRITE & WRITE) !== 0;
const hasExec = (READ_WRITE & EXEC) !== 0;
{hasWrite, hasExec}
```

**Result:**
```
"{hasWrite: true, hasExec: false}"
```

