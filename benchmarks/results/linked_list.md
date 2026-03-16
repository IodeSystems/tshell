# linked_list

**Status:** PASS
**Duration:** 6368ms
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

let to_array = (node) => {
  let result = [];
  while (node) {
    result.push(node.value);
    node = node.next;
  }
  return result;
};

to_array(list);
```

**Result:**
```
"[10, 20, 30]"
```

