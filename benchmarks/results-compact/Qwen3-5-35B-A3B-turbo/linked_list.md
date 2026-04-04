# linked_list

**Status:** PASS
**Duration:** 6014ms
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
export let list = {value: 10, next: {value: 20, next: {value: 30, next: null}}};

let toArray = (node) => {
  let result = [];
  while (node != null) {
    result.push(node.value);
    node = node.next;
  }
  return result;
};

toArray(list);
```

**Result:**
```
"[10, 20, 30]"
```

