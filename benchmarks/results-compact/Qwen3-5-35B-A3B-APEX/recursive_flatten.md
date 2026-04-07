# recursive_flatten

**Status:** PASS
**Duration:** 6710ms
**Tool calls:** 1

## Prompt

> In tshell, write a recursive function that flattens a nested array like [[1,[2]],[[3,4],[5]]] into [1,2,3,4,5].

## Final Answer

```
[1, 2, 3, 4, 5]
```

## Attempts

### Attempt 1 (OK)

```javascript
let flatten = (arr) => {
  let result = [];
  let helper = (a) => {
    for (let item of a) {
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
flatten(nested)
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

