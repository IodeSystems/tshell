# event_emitter

**Status:** PASS
**Duration:** 16794ms
**Tool calls:** 2

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event: one that returns data as-is, one that returns data * 2. Emit with value 42, collect all handler results into an array. Return the array.

## Final Answer

```
[42, 84]
```

## Attempts

### Attempt 1 (OK)

```javascript
let eventEmitter = (() => {
  let handlers = {};
  
  return {
    on: function(event, handler) {
      if (!handlers[event]) {
        handlers[event] = [];
      }
      handlers[event].push(handler);
      return this;
    },
    emit: function(event, data) {
      let results = [];
      if (handlers[event]) {
        for (let i = 0; i < handlers[event].length; i++) {
          let result = handlers[event][i](data);
          results.push(result);
        }
      }
      return results;
    }
  };
})();

eventEmitter.on("data", d => d);
eventEmitter.on("data", d => d * 2);

eventEmitter.emit("data", 42);
```

**Result:**
```
"ERROR: tshell does not support 'this' — there are no classes or methods\n\n  at line 25:15"
```

### Attempt 2 (OK)

```javascript
let eventEmitter = (() => {
  let handlers = {};
  
  return {
    on: function(event, handler) {
      if (!handlers[event]) {
        handlers[event] = [];
      }
      handlers[event].push(handler);
      return eventEmitter;
    },
    emit: function(event, data) {
      let results = [];
      if (handlers[event]) {
        for (let i = 0; i < handlers[event].length; i++) {
          let result = handlers[event][i](data);
          results.push(result);
        }
      }
      return results;
    }
  };
})();

eventEmitter.on("data", d => d);
eventEmitter.on("data", d => d * 2);

eventEmitter.emit("data", 42);
```

**Result:**
```
"[42, 84]"
```

