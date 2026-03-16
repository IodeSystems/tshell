# bitwise_flags

**Status:** PASS
**Duration:** 5842ms
**Tool calls:** 1

## Prompt

> In tshell, define permission flags: READ=4, WRITE=2, EXEC=1. Combine READ|WRITE into a variable. Check if it has WRITE (using &), check if it has EXEC. Return an object {hasWrite: true/false, hasExec: true/false}.

## Final Answer

```
{hasWrite: true, hasExec: false}
```

## Attempts

### Attempt 1 (OK)

```javascript
let READ = 4;
let WRITE = 2;
let EXEC = 1;
let READ_WRITE = READ | WRITE;
let hasWrite = (READ_WRITE & WRITE) !== 0;
let hasExec = (READ_WRITE & EXEC) !== 0;
({hasWrite, hasExec})
```

**Result:**
```
"{hasWrite: true, hasExec: false}"
```

