# binary_search

**Status:** PASS
**Duration:** 6504ms
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
let left = 0, right = arr.length - 1;
while (left <= right) {
  let mid = Math.floor((left + right) / 2);
  if (arr[mid] === target) return mid;
  if (arr[mid] < target) left = mid + 1;
  else right = mid - 1;
}
return -1;
```

**Result:**
```
"3"
```

