# recursive_flatten

**Status:** PASS
**Duration:** 5228ms
**Tool calls:** 1

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
  return arr.flatMap(item => {
    if (isArray(item)) {
      return flatten(item);
    }
    return [item];
  });
}

let result = flatten([[1,[2]],[[3,4],[5]]]);
result
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

