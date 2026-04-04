# linked_list

**Status:** PASS
**Duration:** 6470ms
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
let head = {value: 10, next: null};
let second = {value: 20, next: null};
let third = {value: 30, next: null};

head.next = second;
second.next = third;

function listToArray(node, arr = []) {
  while (node != null) {
    arr.push(node.value);
    node = node.next;
  }
  return arr;
}

listToArray(head)
```

**Result:**
```
"[10, 20, 30]"
```

