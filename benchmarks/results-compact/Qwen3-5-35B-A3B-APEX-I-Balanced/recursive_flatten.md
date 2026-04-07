# recursive_flatten

**Status:** PASS
**Duration:** 6055ms
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
let flatten = (arr, acc = []) => {
    for (let x of arr) {
        if (isArray(x)) {
            flatten(x, acc);
        } else {
            acc.push(x);
        }
    }
    return acc;
};

flatten([[1,[2]],[[3,4],[5]]]);
```

**Result:**
```
"[1, 2, 3, 4, 5]"
```

