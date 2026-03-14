# deep_clone

**Status:** PASS
**Duration:** 14878ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that deep-clones a nested object. Clone {a:1,b:{c:2,d:[3,4]}} and modify the clone's b.c to 99. Return both original and clone to prove they are independent.

## Final Answer

```
The function successfully deep-cloned the nested object. Here's what happened:

1. **Original object**: `{a: 1, b: {c: 2, d: [3, 4]}}`
2. **Clone**: `{a: 1, b: {c: 99, d: [3, 4]}}`

The output proves they are independent:
- The original's `b.c` remains `2`
- The clone's `b.c` was modified to `99`

The `deepClone` function works by:
- Returning primitives (null, numbers, strings, etc.) as-is
- Recursively cloning arrays with `map`
- Recursively cloning objects by iterating through keys and cloning each value
- This ensures all nested levels (objects and arrays) are deeply copied, not just shallow references
```

## Attempts

### Attempt 1 (OK)

```javascript
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') {
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

let original = {a: 1, b: {c: 2, d: [3, 4]}};
let clone = deepClone(original);

clone.b.c = 99;

[original, clone];
```

**Result:**
```
"[{a: 1, b: {c: 2, d: [3, 4]}}, {a: 1, b: {c: 99, d: [3, 4]}}]"
```

