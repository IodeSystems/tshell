# merge_sort

**Status:** PASS
**Duration:** 13351ms
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
let merge = (left, right) => {
  let result = [];
  let i = 0, j = 0;
  while (i < left.len() && j < right.len()) {
    if (left[i] <= right[j]) {
      result = result.concat([left[i]]);
      i++;
    } else {
      result = result.concat([right[j]]);
      j++;
    }
  }
  while (i < left.len()) {
    result = result.concat([left[i]]);
    i++;
  }
  while (j < right.len()) {
    result = result.concat([right[j]]);
    j++;
  }
  return result;
};

let mergeSort = (arr) => {
  if (arr.len() <= 1) {
    return arr;
  }
  let mid = Math.floor(arr.len() / 2);
  let left = arr.slice(0, mid);
  let right = arr.slice(mid);
  return merge(mergeSort(left), mergeSort(right));
};

mergeSort([38, 27, 43, 3, 9, 82, 10])
```

**Result:**
```
"[3, 9, 10, 27, 38, 43, 82]"
```

