# merge_sort

**Status:** PASS
**Duration:** 12531ms
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
const mergeSort = arr => {
  if (arr.len() <= 1) return arr;
  const mid = Math.floor(arr.len() / 2);
  const left = arr.slice(0, mid) |> mergeSort;
  const right = arr.slice(mid) |> mergeSort;
  return merge(left, right);
};

const merge = (left, right) => {
  const result = [];
  let i = 0, j = 0;
  while (i < left.len() && j < right.len()) {
    if (left[i] <= right[j]) {
      result.push(left[i]);
      i++;
    } else {
      result.push(right[j]);
      j++;
    }
  }
  while (i < left.len()) {
    result.push(left[i]);
    i++;
  }
  while (j < right.len()) {
    result.push(right[j]);
    j++;
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

