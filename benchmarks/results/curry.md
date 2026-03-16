# curry

**Status:** PASS
**Duration:** 20563ms
**Tool calls:** 7

## Prompt

> In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3). Should return 8.

## Final Answer

```
8
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let curriedAdd = (a) => (b) => a + b;
let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 2 (ERROR)

```javascript
export let curriedAdd = (a) => (b) => a + b;
export let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 3 (ERROR)

```javascript
let curriedAdd = (a) => (b) => a + b;
let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 4 (ERROR)

```javascript
let curriedAdd = function(a) { return function(b) { return a + b; }; };
let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 5 (ERROR)

```javascript
let curriedAdd = function(a) { return function(b) { return a + b; }; };
let add5 = curriedAdd(5);
return (add5(3));
```

**Result:**
```
"ERROR: null"
```

### Attempt 6 (ERROR)

```javascript
let curriedAdd = function(a) { return function(b) { return a + b; }; };
let add5 = curriedAdd(5);
return add5(3);
```

**Result:**
```
"ERROR: null"
```

### Attempt 7 (OK)

```javascript
let curriedAdd = function(a) { return function(b) { return a + b; }; };
let add5 = curriedAdd(5);
print(add5(3));
```

**Result:**
```
"8"
```

