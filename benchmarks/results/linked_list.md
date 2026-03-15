# linked_list

**Status:** PASS
**Duration:** 9936ms
**Tool calls:** 2

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

function toArray(node, acc = []) {
  if (node === null) return acc;
  acc.push(node.value);
  return toArray(node.next, acc);
}

toArray(list);
```

**Result:**
```
"ERROR: Type mismatch in member access .push\n\n  Expected: object, array, or string\n  Got:      null (null)\n\n  at line 5:5"
```

### Attempt 2 (OK)

```javascript
let list = {value: 10, next: {value: 20, next: {value: 30, next: null}}};

function toArray(node) {
  if (node === null) return [];
  let rest = toArray(node.next);
  rest.unshift(node.value);
  return rest;
}

toArray(list);
```

**Result:**
```
"[10, 20, 30]"
```

