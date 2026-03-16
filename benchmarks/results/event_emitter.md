# event_emitter

**Status:** PASS
**Duration:** 13364ms
**Tool calls:** 2

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event: one that returns data as-is, one that returns data * 2. Emit with value 42, collect all handler results into an array. Return the array.

## Final Answer

```
[42, 84]
```

## Attempts

### Attempt 1 (ERROR)

```javascript
export let EventEmitter = (function() {
  let events = {};
  
  return {
    on(event, handler) {
      if (!events[event]) events[event] = [];
      events[event].push(handler);
    },
    emit(event, data) {
      if (!events[event]) return [];
      return events[event].map(handler => handler(data));
    }
  };
})();

let emitter = EventEmitter();
emitter.on("data", d => d);
emitter.on("data", d => d * 2);
emitter.emit("data", 42);
```

**Result:**
```
"ERROR: Syntax error at line 5:6\n\n      on(event, handler) {\n        ^\n\n  Expected '}' to close the block or object"
```

### Attempt 2 (OK)

```javascript
export let events = {};

export let on = function(event, handler) {
  if (!events[event]) events[event] = [];
  events[event].push(handler);
};

export let emit = function(event, data) {
  if (!events[event]) return [];
  return events[event].map(function(handler) {
    return handler(data);
  });
};

on("data", function(d) {
  return d;
});

on("data", function(d) {
  return d * 2;
});

emit("data", 42);
```

**Result:**
```
"[42, 84]"
```

