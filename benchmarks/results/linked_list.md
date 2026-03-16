# linked_list

**Status:** PASS
**Duration:** 10072ms
**Tool calls:** 2

## Prompt

> In tshell, implement a singly linked list using nested objects {value, next}. Build a list of [10, 20, 30], then write a function to convert it to an array. Return the array.

## Final Answer

```
[10, 20, 30]
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let head = {value: 10, next: {value: 20, next: {value: 30, next: null}}};
let toList = (node, acc = []) => (node === null ? acc : toList(node.next, [...acc, node.value]));
toList(head)
```

**Result:**
```
"ERROR: Type mismatch in spread\n\n  Expected: array\n  Got:      null (null)\n\n  Hint: ... can only spread arrays into arrays\n\n  at line 2:62"
```

### Attempt 2 (OK)

```javascript
let head = {value: 10, next: {value: 20, next: {value: 30, next: null}}};
let toList = (node, acc) => {
  if (node === null) return acc;
  if (acc === null) acc = [];
  return toList(node.next, [...acc, node.value]);
};
toList(head, null)
```

**Result:**
```
"[10, 20, 30]"
```

