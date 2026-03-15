# recursive_flatten

**Status:** PASS
**Duration:** 10600ms
**Tool calls:** 2

## Prompt

> In tshell, write a recursive function that flattens a nested array like [[1,[2]],[[3,4],[5]]] into [1,2,3,4,5]. Return the flattened result.

## Final Answer

```
[1, 2, 3, 4, 5]
```

## Attempts

### Attempt 1 (OK)

```javascript
function flatten(arr) {
  return arr.reduce((acc, val) => {
    if (isArray(val)) {
      return acc.concat(flatten(val));
    } else {
      return acc.concat(val);
    }
  }, []);
}

flatten([[1,[2]],[[3,4],[5]]])
```

**Result:**
```
"ERROR: Type mismatch in pipe into concat\n\n  Expected: array\n  Got:      number (1)\n\n  at line 6:23"
```

### Attempt 2 (OK)

```javascript
function flatten(arr) {
  let result = [];
  for (let i = 0; i < arr.length; i++) {
    let val = arr[i];
    if (isArray(val)) {
      result = result.concat(flatten(val));
    } else {
      result.push(val);
    }
  }
  return result;
}

flatten([[1,[2]],[[3,4],[5]]])
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

