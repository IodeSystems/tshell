# linked_list

**Status:** PASS
**Duration:** 6216ms
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
let list = {value: 10, next: {value: 20, next: {value: 30, next: null}}};

let toArray = (head) => {
  let result = [];
  let current = head;
  while (current !== null) {
    result.push(current.value);
    current = current.next;
  }
  return result;
};

toArray(list)
```

**Result:**
```
"[10, 20, 30]"
```

