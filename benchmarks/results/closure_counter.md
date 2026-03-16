# closure_counter

**Status:** PASS
**Duration:** 5315ms
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
const createCounter = () => {
  let count = 0;
  return {
    increment: () => { count++; },
    get: () => count
  };
};

const counter = createCounter();
counter.increment();
counter.increment();
counter.increment();
counter.increment();
counter.increment();
counter.get();
```

**Result:**
```
"5"
```

