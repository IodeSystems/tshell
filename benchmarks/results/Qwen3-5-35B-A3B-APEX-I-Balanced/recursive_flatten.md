# recursive_flatten

**Status:** PASS
**Duration:** 6791ms
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
function flatten(arr) {
  let result = [];
  for (let x of arr) {
    if (isArray(x)) {
      result = concat(result, flatten(x));
    } else {
      result.push(x);
    }
  }
  return result;
}

let nested = [[1, [2]], [[3, 4], [5]]];
flatten(nested);
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

