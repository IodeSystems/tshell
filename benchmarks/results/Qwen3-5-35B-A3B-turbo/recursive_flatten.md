# recursive_flatten

**Status:** PASS
**Duration:** 5773ms
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
  for (let item of arr) {
    if (isArray(item)) {
      result = result.concat(flatten(item));
    } else {
      result.push(item);
    }
  }
  return result;
};

let nested = [[1,[2]],[[3,4],[5]]];
flatten(nested)
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

