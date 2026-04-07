# closure_counter

**Status:** PASS
**Duration:** 5281ms
**Tool calls:** 1

## Prompt

> In tshell, create a counter using closures: a function that returns an object with increment() and get() methods. Call increment 5 times, return get().

## Final Answer

```
5
```

## Attempts

### Attempt 1 (OK)

```javascript
function makeCounter() {
  let count = 0;
  return {
    increment: () => { count++; },
    get: () => count
  };
}
let c = makeCounter();
c.increment();
c.increment();
c.increment();
c.increment();
c.increment();
c.get()
```

**Result:**
```
"5"
```

