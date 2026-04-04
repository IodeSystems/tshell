# event_emitter

**Status:** PASS
**Duration:** 6337ms
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
let events = {};
let on = (event, handler) => {
  if (!events[event]) events[event] = [];
  events[event].push(handler);
};
let emit = (event, data) => {
  if (!events[event]) return [];
  return events[event].map(h => h(data));
};
on(
```

**Result:**
```
"[42, 84]"
```

