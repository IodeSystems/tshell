# recursive_flatten

**Status:** PASS
**Duration:** 8369ms
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
  return arr.reduce((acc, x) => {
    if (isArray(x)) {
      return concat(acc, flatten(x));
    } else {
      return concat(acc, [x]);
    }
  }, []);
}

export function flatten(arr) {
  return arr.reduce((acc, x) => {
    if (isArray(x)) {
      return concat(acc, flatten(x));
    } else {
      return concat(acc, [x]);
    }
  }, []);
}

let input = [[1,[2]],[[3,4],[5]]];
flatten(input);
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

