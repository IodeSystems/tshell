# event_emitter

**Status:** PASS
**Duration:** 7270ms
**Tool calls:** 1

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event: one that returns data as-is, one that returns data * 2. Emit with value 42, collect all handler results into an array. Return the array.

## Final Answer

```
[42, 84]
```

## Attempts

### Attempt 1 (OK)

```javascript
let handlers = {};

function on(event, handler) {
  if (!handlers[event]) handlers[event] = [];
  handlers[event].push(handler);
}

function emit(event, data) {
  let results = [];
  if (handlers[event]) {
    handlers[event].forEach(h => results.push(h(data)));
  }
  return results;
}

on('data', x => x);
on('data', x => x * 2);

emit('data', 42);
```

**Result:**
```
"[42, 84]"
```

