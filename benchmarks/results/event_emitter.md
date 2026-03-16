# event_emitter

**Status:** PASS
**Duration:** 13234ms
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
let handlers = {}
let on = (event, handler) => {
  if (!handlers[event]) handlers[event] = []
  handlers[event].push(handler)
  return this
}
let emit = (event, data) => {
  let results = []
  if (handlers[event]) {
    for (let handler of handlers[event]) {
      results.push(handler(data))
    }
  }
  return results
}

on("data", d => d)
on("data", d => d * 2)
emit("data", 42)
```

**Result:**
```
"ERROR: tshell does not support 'this' — there are no classes or methods\n\n  at line 17:2"
```

### Attempt 2 (OK)

```javascript
let handlers = {}
let on = (event, handler) => {
  if (!handlers[event]) handlers[event] = []
  handlers[event].push(handler)
  return on
}
let emit = (event, data) => {
  let results = []
  if (handlers[event]) {
    for (let handler of handlers[event]) {
      results.push(handler(data))
    }
  }
  return results
}

on("data", d => d)
on("data", d => d * 2)
emit("data", 42)
```

**Result:**
```
"[42, 84]"
```

