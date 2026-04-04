# recursive_flatten

**Status:** PASS
**Duration:** 5789ms
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
  let rec = (x) => {
    if (isArray(x)) {
      for (let item of x) {
        rec(item);
      }
    } else {
      result.push(x);
    }
  };
  rec(arr);
  return result;
};
flatten([[1,[2]],[[3,4],[5]]])
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

