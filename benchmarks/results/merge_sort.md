# merge_sort

**Status:** PASS
**Duration:** 12478ms
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
const mergeSort = (arr) => {
  if (arr.len() <= 1) return arr;
  const mid = Math.floor(arr.len() / 2);
  const left = mergeSort(arr.slice(0, mid));
  const right = mergeSort(arr.slice(mid));
  
  const merge = (a, b) => {
    const result = [];
    let i = 0, j = 0;
    while (i < a.len() && j < b.len()) {
      if (a[i] <= b[j]) {
        result.push(a[i]);
        i++;
      } else {
        result.push(b[j]);
        j++;
      }
    }
    while (i < a.len()) {
      result.push(a[i]);
      i++;
    }
    while (j < b.len()) {
      result.push(b[j]);
      j++;
    }
    return result;
  };
  
  return merge(left, right);
};

mergeSort([38, 27, 43, 3, 9, 82, 10]);
```

**Result:**
```
"[3, 9, 10, 27, 38, 43, 82]"
```

