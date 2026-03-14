# recursive_flatten

**Status:** PASS
**Duration:** 9769ms
**Tool calls:** 1

## Prompt

> In tshell, write a recursive function that flattens a nested array like [[1,[2]],[[3,4],[5]]] into [1,2,3,4,5]. Return the flattened result.

## Final Answer

```
The recursive function successfully flattens the nested array `[[1,[2]],[[3,4],[5]]]` into `[1, 2, 3, 4, 5]`.

The function `flatten` uses an inner `helper` function that:
1. Checks if an item is an array using `isArray()`
2. If it's an array, recursively calls `helper` on each element using `forEach`
3. If it's not an array (a primitive), pushes it to the `result` array
4. Finally returns the flattened `result` array
```

## Attempts

### Attempt 1 (OK)

```javascript
export function flatten(arr) {
  let result = [];
  
  function helper(item) {
    if (isArray(item)) {
      forEach(item, helper);
    } else {
      result.push(item);
    }
  }
  
  helper(arr);
  return result;
}

flatten([[1,[2]],[[3,4],[5]]])
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

