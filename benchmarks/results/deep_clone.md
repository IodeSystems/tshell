# deep_clone

**Status:** PASS
**Duration:** 34553ms
**Tool calls:** 4

## Prompt

> In tshell, write a function that deep-clones a nested object. Clone {a:1,b:{c:2,d:[3,4]}} and modify the clone's b.c to 99. Return both original and clone to prove they are independent.

## Final Answer

```
[{a: 1, b: {c: 2, d: [3, 4]}}, {a: 1, b: {c: 99, d: [3, 4]}}]
```

## Attempts

### Attempt 1 (ERROR)

```javascript
function deepClone(obj) {
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  if (Array.isArray(obj)) {
    return obj.map(item => deepClone(item));
  }
  const cloned = {};
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      cloned[key] = deepClone(obj[key]);
    }
  }
  return cloned;
}

const original = {a:1,b:{c:2,d:[3,4]}};
const clone = deepClone(original);
clone.b.c = 99;

return [original, clone];
```

**Result:**
```
"ERROR: null"
```

### Attempt 2 (ERROR)

```javascript
function deepClone(obj) {
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  if (Array.isArray(obj)) {
    return obj.map(item => deepClone(item));
  }
  const cloned = {};
  const keys = Object.keys(obj);
  for (let i = 0; i < keys.length; i++) {
    const key = keys[i];
    cloned[key] = deepClone(obj[key]);
  }
  return cloned;
}

const original = {a:1,b:{c:2,d:[3,4]}};
const clone = deepClone(original);
clone.b.c = 99;

return [original, clone];
```

**Result:**
```
"ERROR: null"
```

### Attempt 3 (ERROR)

```javascript
function deepClone(obj) {
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  if (Array.isArray(obj)) {
    let arr = [];
    for (let i = 0; i < obj.length; i++) {
      arr[i] = deepClone(obj[i]);
    }
    return arr;
  }
  let cloned = {};
  let keys = Object.keys(obj);
  for (let i = 0; i < keys.length; i++) {
    let key = keys[i];
    cloned[key] = deepClone(obj[key]);
  }
  return cloned;
}

let original = {a:1,b:{c:2,d:[3,4]}};
let clone = deepClone(original);
clone.b.c = 99;

return [original, clone];
```

**Result:**
```
"ERROR: null"
```

### Attempt 4 (OK)

```javascript
function deepClone(obj) {
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  if (Array.isArray(obj)) {
    let arr = [];
    for (let i = 0; i < obj.length; i++) {
      arr[i] = deepClone(obj[i]);
    }
    return arr;
  }
  let cloned = {};
  let keys = Object.keys(obj);
  for (let i = 0; i < keys.length; i++) {
    let key = keys[i];
    cloned[key] = deepClone(obj[key]);
  }
  return cloned;
}

let original = {a:1,b:{c:2,d:[3,4]}};
let clone = deepClone(original);
clone.b.c = 99;

let result = [original, clone];
result;
```

**Result:**
```
"[{a: 1, b: {c: 2, d: [3, 4]}}, {a: 1, b: {c: 99, d: [3, 4]}}]"
```

