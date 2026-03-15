# linked_list

**Status:** PASS
**Duration:** 11351ms
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
const list = { value: 10, next: { value: 20, next: { value: 30, next: null } } };

const listToArray = (head) => {
  let result = [];
  let current = head;
  while (current !== null && current !== undefined) {
    result.push(current.value);
    current = current.next;
  }
  return result;
};

listToArray(list);
```

**Result:**
```
"ERROR: tshell does not have 'undefined' — use null instead\n\n  at line 13:11"
```

### Attempt 2 (OK)

```javascript
const list = { value: 10, next: { value: 20, next: { value: 30, next: null } } };

const listToArray = (head) => {
  let result = [];
  let current = head;
  while (current !== null) {
    result.push(current.value);
    current = current.next;
  }
  return result;
};

listToArray(list);
```

**Result:**
```
"[10, 20, 30]"
```

