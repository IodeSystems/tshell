# curry

**Status:** PASS
**Duration:** 14182ms
**Tool calls:** 4

## Prompt

> In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3). Should return 8.

## Final Answer

```
8
```

## Attempts

### Attempt 1 (OK)

```javascript
export function curriedAdd(a) {
  return (b) => a + b;
}

let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 2 (OK)

```javascript
export let curriedAdd = (a) => (b) => a + b;

let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 3 (OK)

```javascript
export function curriedAdd(a) {
  return function(b) {
    return a + b;
  };
}

let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 4 (OK)

```javascript
export function curriedAdd(a) {
  return function(b) {
    return a + b;
  };
}

let add5 = curriedAdd(5);
add5(3);
```

**Result:**
```
"8"
```

