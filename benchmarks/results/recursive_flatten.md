# recursive_flatten

**Status:** PASS
**Duration:** 7023ms
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
const flatten = (arr) => {
  const result = [];
  const helper = (items) => {
    for (let item of items) {
      if (isArray(item)) {
        helper(item);
      } else {
        result.push(item);
      }
    }
  };
  helper(arr);
  return result;
};

let nested = [[1, [2]], [[3, 4], [5]]];
flatten(nested);
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

