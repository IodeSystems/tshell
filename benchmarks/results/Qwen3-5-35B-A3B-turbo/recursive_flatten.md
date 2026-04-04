# recursive_flatten

**Status:** PASS
**Duration:** 5842ms
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
  if (!isArray(arr)) {
    return [arr];
  }
  let result = [];
  for (let item of arr) {
    result = concat(result, flatten(item));
  }
  return result;
};

let nested = [[1,[2]],[[3,4],[5]]];
let flat = flatten(nested);
flat
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

