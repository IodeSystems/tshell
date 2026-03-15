# recursive_flatten

**Status:** PASS
**Duration:** 6396ms
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
let flatten = (arr) => {
  let result = [];
  for (let item of arr) {
    if (isArray(item)) {
      result = concat(result, flatten(item));
    } else {
      result = concat(result, [item]);
    }
  }
  return result;
};

flatten([[1, [2]], [[3, 4], [5]]])
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

