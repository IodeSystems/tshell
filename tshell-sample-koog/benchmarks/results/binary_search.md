# binary_search

**Status:** PASS
**Duration:** 8609ms
**Tool calls:** 1

## Prompt

> In tshell, implement binary search on a sorted array. Search for 7 in [1,3,5,7,9,11,13,15]. Return the index (should be 3).

## Final Answer

```
3
```

## Attempts

### Attempt 1 (OK)

```javascript
export function binarySearch(arr, target) {
  let left = 0;
  let right = arr.length - 1;
  
  while (left <= right) {
    let mid = Math.floor((left + right) / 2);
    if (arr[mid] === target) {
      return mid;
    } else if (arr[mid] < target) {
      left = mid + 1;
    } else {
      right = mid - 1;
    }
  }
  
  return -1;
}

let arr = [1,3,5,7,9,11,13,15];
let target = 7;
binarySearch(arr, target);
```

**Result:**
```
"3"
```

