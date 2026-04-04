# merge_sort

**Status:** PASS
**Duration:** 11687ms
**Tool calls:** 1

## Prompt

> In tshell, implement merge sort. Sort the array [38, 27, 43, 3, 9, 82, 10].

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
  let left = slice(arr, 0, mid);
  let right = slice(arr, mid);
  return merge(mergeSort(left), mergeSort(right));
};

let merge = (left, right) => {
  let result = [];
  let i = 0;
  let j = 0;
  while (i < len(left) && j < len(right)) {
    if (left[i] <= right[j]) {
      result = concat(result, [left[i]]);
      i++;
    } else {
      result = concat(result, [right[j]]);
      j++;
    }
  }
  while (i < len(left)) {
    result = concat(result, [left[i]]);
    i++;
  }
  while (j < len(right)) {
    result = concat(result, [right[j]]);
    j++;
  }
  return result;
};

mergeSort([38, 27, 43, 3, 9, 82, 10]);
```

**Result:**
```
"[3, 9, 10, 27, 38, 43, 82]"
```

