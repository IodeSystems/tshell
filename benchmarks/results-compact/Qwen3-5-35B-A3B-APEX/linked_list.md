# linked_list

**Status:** PASS
**Duration:** 6448ms
**Tool calls:** 1

## Prompt

> In tshell, implement a singly linked list using nested objects {value, next}. Build a list of [10, 20, 30], then write a function to convert it to an array.

## Final Answer

```
[10, 20, 30]
```

## Attempts

### Attempt 1 (OK)

```javascript
let linkedList = { value: 10, next: { value: 20, next: { value: 30, next: null } } };

let listToArray = (node) => {
  let result = [];
  let current = node;
  while (current !== null) {
    result.push(current.value);
    current = current.next;
  }
  return result;
};

listToArray(linkedList);
```

**Result:**
```
"[10, 20, 30]"
```

