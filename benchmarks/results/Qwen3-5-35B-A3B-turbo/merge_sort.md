# merge_sort

**Status:** PASS
**Duration:** 10761ms
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
function mergeSort(arr) {
  if (arr.len() <= 1) return arr;
  let mid = Math.floor(arr.len() / 2);
  let left = mergeSort(arr.slice(0, mid));
  let right = mergeSort(arr.slice(mid));
  let result = [];
  let i = 0;
  let j = 0;
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
}

mergeSort([38, 27, 43, 3, 9, 82, 10])
```

**Result:**
```
"[3, 9, 10, 27, 38, 43, 82]"
```

