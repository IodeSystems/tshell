# event_emitter

**Status:** PASS
**Duration:** 7637ms
**Tool calls:** 1

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event: one that returns data as-is, one that returns data * 2. Emit with value 42, collect all handler results into an array.

## Final Answer

```
[42, 84]
```

## Attempts

### Attempt 1 (OK)

```javascript
let handlers = {};

let on = (event, handler) => {
  if (!handlers[event]) handlers[event] = [];
  handlers[event].push(handler);
};

let emit = (event, data) => {
  let results = [];
  if (handlers[event]) {
    for (let handler of handlers[event]) {
      results.push(handler(data));
    }
  }
  return results;
};

on("data", d => d);
on("data", d => d * 2);
let result = emit("data", 42);
result
```

**Result:**
```
"[42, 84]"
```

