# event_emitter

**Status:** PASS
**Duration:** 19361ms
**Tool calls:** 2

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event, emit it with value 42, collect all handler results into an array. Return the array.

## Final Answer

```
The event emitter implementation works correctly. The results are:

- First handler: `42 * 2 = 84`
- Second handler: `42 * 3 = 126`

The returned array is `[84, 126]`.
```

## Attempts

### Attempt 1 (OK)

```javascript
export function EventEmitter() {
  let handlers = {};
  
  function on(event, handler) {
    if (!handlers[event]) {
      handlers[event] = [];
    }
    handlers[event].push(handler);
    return this;
  }
  
  function emit(event, data) {
    if (!handlers[event]) {
      return [];
    }
    return handlers[event].map(fn => fn(data));
  }
  
  return { on, emit };
}

let emitter = EventEmitter();

let result1 = null;
let result2 = null;

emitter.on('data', x => {
  result1 = x * 2;
  return result1;
});

emitter.on('data', x => {
  result2 = x * 3;
  return result2;
});

let results = emitter.emit('data', 42);
results
```

**Result:**
```
"ERROR: tshell does not support 'this' — there are no classes or methods\n\n  at line 27:10"
```

### Attempt 2 (OK)

```javascript
export function EventEmitter() {
  let handlers = {};
  
  function on(event, handler) {
    if (!handlers[event]) {
      handlers[event] = [];
    }
    handlers[event].push(handler);
  }
  
  function emit(event, data) {
    if (!handlers[event]) {
      return [];
    }
    return handlers[event].map(fn => fn(data));
  }
  
  return { on: on, emit: emit };
}

let emitter = EventEmitter();

emitter.on('data', x => x * 2);
emitter.on('data', x => x * 3);

emitter.emit('data', 42)
```

**Result:**
```
"[84, 126]"
```

