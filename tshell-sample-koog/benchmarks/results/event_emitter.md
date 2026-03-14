# event_emitter

**Status:** PASS
**Duration:** 14079ms
**Tool calls:** 1

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event, emit it with value 42, collect all handler results into an array. Return the array.

## Final Answer

```
The event emitter implementation works correctly. Here's what happened:

1. Created an `EventEmitter` with `on(event, handler)` and `emit(event, data)` methods
2. Registered two handlers for the 'data' event:
   - First handler: multiplies the value by 2
   - Second handler: adds 10 to the value
3. Emitted the 'data' event with value 42
4. Collected all handler results into an array: `[84, 52]`

- First handler: 42 * 2 = 84
- Second handler: 42 + 10 = 52
```

## Attempts

### Attempt 1 (OK)

```javascript
// Simple event emitter implementation
function EventEmitter() {
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
    return handlers[event].map(h => h(data));
  }
  
  return { on, emit };
}

// Create an event emitter
let emitter = EventEmitter();

// Register two handlers for 'data' event
emitter.on('data', (value) => value * 2);
emitter.on('data', (value) => value + 10);

// Emit the event with value 42
let results = emitter.emit('data', 42);

results
```

**Result:**
```
"[84, 52]"
```

