# merge_sort

**Status:** PASS
**Duration:** 14227ms
**Tool calls:** 1

## Prompt

> In tshell, implement merge sort. Sort the array [38, 27, 43, 3, 9, 82, 10]. Return the sorted array.

## Final Answer

```
[3, 9, 10, 27, 38, 43, 82]
```

## Attempts

### Attempt 1 (OK)

```javascript
let mergeSort = (arr) => {
  if (len(arr) <= 1) return arr;
  let mid = floor(len(arr) / 2);
  let left = mergeSort(slice(arr, 0, mid));
  let right = mergeSort(slice(arr, mid));
  return merge(left, right);
};

let merge = (left, right) => {
  let result = [];
  let i = 0;
  let j = 0;
  while (i < len(left) && j < len(right)) {
    if (left[i] <= right[j]) {
      result = concat(result, [left[i]]);
      i = i + 1;
    } else {
      result = concat(result, [right[j]]);
      j = j + 1;
    }
  }
  while (i < len(left)) {
    result = concat(result, [left[i]]);
    i = i + 1;
  }
  while (j < len(right)) {
    result = concat(result, [right[j]]);
    j = j + 1;
  }
  return result;
};

let arr = [38, 27, 43, 3, 9, 82, 10];
mergeSort(arr);
```

**Result:**
```
"[3, 9, 10, 27, 38, 43, 82]"
```

