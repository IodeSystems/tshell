# binary_search

**Status:** PASS
**Duration:** 8849ms
**Tool calls:** 1

## Prompt

> In tshell, implement binary search on a sorted array. Search for 7 in [1,3,5,7,9,11,13,15].

## Final Answer

```
3
```

## Attempts

### Attempt 1 (OK)

```javascript
let arr = [1,3,5,7,9,11,13,15];
let target = 7;
let low = 0;
let high = arr.length - 1;

while (low <= high) {
    let mid = Math.floor((low + high) / 2);
    if (arr[mid] === target) {
        break;
    } else if (arr[mid] < target) {
        low = mid + 1;
    } else {
        high = mid - 1;
    }
}

if (low > high) {
    -1;
} else {
    mid;
}
```

**Result:**
```
"3"
```

