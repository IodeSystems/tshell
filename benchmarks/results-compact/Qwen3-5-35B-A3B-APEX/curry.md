# curry

**Status:** PASS
**Duration:** 5047ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3).

## Final Answer

```
8
```

## Attempts

### Attempt 1 (OK)

```javascript
let curriedAdd = (fn) => (a) => (b) => fn(a, b);
let add = (a, b) => a + b;
let curriedAdd2 = curriedAdd(add);
let add5 = curriedAdd2(5);
return add5(3);
```

**Result:**
```
"8"
```

