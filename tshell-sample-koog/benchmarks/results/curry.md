# curry

**Status:** PASS
**Duration:** 9085ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3). Should return 8.

## Final Answer

```
The function works correctly! Here's what I created:

```tshell
export function curriedAdd(a) {
  return function(b) {
    return a + b;
  };
}

let add5 = curriedAdd(5);
add5(3)
```

The result is `8`, as expected. The `curriedAdd` function takes the first argument `a` and returns a new function that takes the second argument `b`, allowing you to partially apply the addition function. When you call `curriedAdd(5)`, it returns a function that adds 5 to its argument. Then `add5(3)` adds 5 + 3 = 8.
```

## Attempts

### Attempt 1 (OK)

```javascript
export function curriedAdd(a) {
  return function(b) {
    return a + b;
  };
}

let add5 = curriedAdd(5);
add5(3)
```

**Result:**
```
"8"
```

