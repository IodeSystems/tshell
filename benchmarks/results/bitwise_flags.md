# bitwise_flags

**Status:** PASS
**Duration:** 5961ms
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
export let READ = 4;
export let WRITE = 2;
export let EXEC = 1;

export let combined = READ | WRITE;

export let result = {
  hasWrite: (combined & WRITE) !== 0,
  hasExec: (combined & EXEC) !== 0
};

result;
```

**Result:**
```
"{hasWrite: true, hasExec: false}"
```

