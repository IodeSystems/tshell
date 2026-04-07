# bitwise_flags

**Status:** PASS
**Duration:** 6112ms
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
export let READ = 4;
export let WRITE = 2;
export let EXEC = 1;

let combined = READ + WRITE;
let hasWrite = (combined & WRITE) !== 0;
let hasExec = (combined & EXEC) !== 0;

{hasWrite, hasExec}
```

**Result:**
```
"{hasWrite: true, hasExec: false}"
```

