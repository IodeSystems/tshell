# recursive_flatten

**Status:** PASS
**Duration:** 20366ms
**Tool calls:** 1

## Prompt

> In tshell, write a recursive function that flattens a nested array like [[1,[2]],[[3,4],[5]]] into [1,2,3,4,5]. Return the flattened result.

## Final Answer

```
Here's a recursive function in tshell that flattens a nested array:

```tshell
function flatten(arr) {
  let result = [];
  
  function helper(a) {
    for (let i = 0; i < a.len(); i++) {
      let item = a[i];
      if (isArray(item)) {
        helper(item);
      } else {
        result.push(item);
      }
    }
  }
  
  helper(arr);
  return result;
}

let nested = [[1, [2]], [[3, 4], [5]]];
flatten(nested)
```

This returns `[1, 2, 3, 4, 5]`.

The function works by:
1. Creating an empty result array
2. Defining a helper function that iterates through each element
3. If an element is an array, it recursively calls itself on that sub-array
4. If an element is not an array, it pushes it to the result
5. Finally returns the flattened array
```

## Attempts

### Attempt 1 (OK)

```javascript
function flatten(arr) {
  let result = [];
  
  function helper(a) {
    for (let i = 0; i < a.len(); i++) {
      let item = a[i];
      if (isArray(item)) {
        helper(item);
      } else {
        result.push(item);
      }
    }
  }
  
  helper(arr);
  return result;
}

let nested = [[1, [2]], [[3, 4], [5]]];
flatten(nested)
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

