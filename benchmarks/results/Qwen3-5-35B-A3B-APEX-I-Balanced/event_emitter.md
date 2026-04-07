# event_emitter

**Status:** PASS
**Duration:** 18913ms
**Tool calls:** 3

## Prompt

> In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event: one that returns data as-is, one that returns data * 2. Emit with value 42, collect all handler results into an array.

## Final Answer

```
[42, 84]
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let EventEmitter = (() => {
  let handlers = {};
  return {
    on(event, handler) {
      if (!handlers[event]) handlers[event] = [];
      handlers[event].push(handler);
    },
    emit(event, data) {
      if (!handlers[event]) return [];
      return handlers[event].map(h => h(data));
    }
  };
})();

let ee = EventEmitter();
ee.on(
```

**Result:**
```
"ERROR: Type mismatch in call

  Expected: function
  Got:      object ({on: function on(event, handler), emit: function emit(event, data)})

  at line 15:21"
```

### Attempt 2 (ERROR)

```javascript
let EventEmitter = (() => {
  let handlers = {};
  return {
    on: function(event, handler) {
      if (!handlers[event]) handlers[event] = [];
      handlers[event].push(handler);
    },
    emit: function(event, data) {
      if (!handlers[event]) return [];
      return handlers[event].map(function(h) { return h(data); });
    }
  };
})();

let ee = EventEmitter();
ee.on(
```

**Result:**
```
"ERROR: Type mismatch in call

  Expected: function
  Got:      object ({on: function <anonymous>(event, handler), emit: function <anonymous>(event, data)})

  at line 15:21"
```

### Attempt 3 (OK)

```javascript
let handlers = {};
let on = function(event, handler) {
  if (!handlers[event]) handlers[event] = [];
  handlers[event].push(handler);
};
let emit = function(event, data) {
  if (!handlers[event]) return [];
  return handlers[event].map(function(h) { return h(data); });
};

on(
```

**Result:**
```
"[42, 84]"
```

