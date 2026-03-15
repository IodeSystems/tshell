# curry

**Status:** PASS
**Duration:** 8419ms
**Tool calls:** 2

## Prompt

> In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3). Should return 8.

## Final Answer

```
8
```

## Attempts

### Attempt 1 (OK)

```javascript
export let curriedAdd = (f) => (a) => (b) => f(a, b);
export let add = (a, b) => a + b;
export let curriedAdd2 = curriedAdd(add);
export let add5 = curriedAdd2(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 2 (OK)

```javascript
let curriedAdd = (f) => (a) => (b) => f(a, b);
let add = (a, b) => a + b;
let curriedAdd2 = curriedAdd(add);
let add5 = curriedAdd2(5);
add5(3);
```

**Result:**
```
"8"
```

