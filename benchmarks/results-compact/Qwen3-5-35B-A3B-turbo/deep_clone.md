# deep_clone

**Status:** PASS
**Duration:** 9066ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that deep-clones a nested object. Clone {a:1,b:{c:2,d:[3,4]}} and modify the clone's b.c to 99. Return both original and clone to prove they are independent.

## Final Answer

```
[{a: 1, b: {c: 2, d: [3, 4]}}, {a: 1, b: {c: 99, d: [3, 4]}}]
```

## Attempts

### Attempt 1 (OK)

```javascript
function deepClone(obj) {
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  if (Array.isArray(obj)) {
    return obj.map(deepClone);
  }
  let cloned = {};
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      cloned[key] = deepClone(obj[key]);
    }
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
"[{a: 1, b: {c: 2, d: [3, 4]}}, {a: 1, b: {c: 99, d: [3, 4]}}]"
```

