# curry

**Status:** PASS
**Duration:** 5235ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3). Should return 8.

## Final Answer

```
8
```

## Attempts

### Attempt 1 (OK)

```javascript
let curriedAdd = (f) => (a) => (b) => f(a, b);
let add = (a, b) => a + b;
let curriedAddFunc = curriedAdd(add);
let add5 = curriedAddFunc(5);
return add5(3);
```

**Result:**
```
"8"
```

