# curry

**Status:** PASS
**Duration:** 4232ms
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
function curriedAdd(a) {
  return b => a + b;
}
let add5 = curriedAdd(5);
add5(3)
```

**Result:**
```
"8"
```

