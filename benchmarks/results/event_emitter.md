# event_emitter

**Status:** PASS
**Duration:** 8721ms
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
let EventEmitter = (() => {
  let handlers = {};
  
  let on = (event, handler) => {
    if (!handlers[event]) {
      handlers[event] = [];
    }
    handlers[event].push(handler);
  };
  
  let emit = (event, data) => {
    if (!handlers[event]) {
      return [];
    }
    return handlers[event].map(handler => handler(data));
  };
  
  return { on, emit };
})();

let results = [];

EventEmitter.on('data', (d) => d);
EventEmitter.on('data', (d) => d * 2);

results = EventEmitter.emit('data', 42);

results
```

**Result:**
```
"[42, 84]"
```

