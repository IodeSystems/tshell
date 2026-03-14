# linked_list

**Status:** PASS
**Duration:** 9396ms
**Tool calls:** 1

## Prompt

> In tshell, implement a singly linked list using nested objects {value, next}. Build a list of [10, 20, 30], then write a function to convert it to an array. Return the array.

## Final Answer

```
[10, 20, 30]
```

## Attempts

### Attempt 1 (OK)

```javascript
export function toList(arr) {
  if (arr.length === 0) return null;
  let head = { value: arr[0], next: null };
  let current = head;
  for (let i = 1; i < arr.length; i++) {
    current.next = { value: arr[i], next: null };
    current = current.next;
  }
  return head;
}

export function listToArray(list) {
  let result = [];
  let current = list;
  while (current !== null) {
    result.push(current.value);
    current = current.next;
  }
  return result;
}

let list = toList([10, 20, 30]);
listToArray(list);
```

**Result:**
```
"[10, 20, 30]"
```

