# recursive_flatten

**Status:** PASS
**Duration:** 6861ms
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
let flatten = arr => {
  let result = [];
  let recurse = item => {
    if (isArray(item)) {
      for (let sub of item) {
        recurse(sub);
      }
    } else {
      result.push(item);
    }
  };
  recurse(arr);
  return result;
};

let nested = [[1, [2]], [[3, 4], [5]]];
flatten(nested);
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

