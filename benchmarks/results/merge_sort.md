# merge_sort

**Status:** PASS
**Duration:** 12866ms
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
  let left = slice(arr, 0, mid) |> mergeSort();
  let right = slice(arr, mid) |> mergeSort();
  
  let merge = (a, b) => {
    let result = [];
    let i = 0;
    let j = 0;
    while (i < len(a) && j < len(b)) {
      if (a[i] <= b[j]) {
        result = concat(result, [a[i]]);
        i = i + 1;
      } else {
        result = concat(result, [b[j]]);
        j = j + 1;
      }
    }
    result = concat(result, slice(a, i));
    result = concat(result, slice(b, j));
    return result;
  };
  
  return merge(left, right);
};

let arr = [38, 27, 43, 3, 9, 82, 10];
mergeSort(arr)
```

**Result:**
```
"[3, 9, 10, 27, 38, 43, 82]"
```

