# scatter_parallel

**Status:** PASS
**Duration:** 3868ms
**Tool calls:** 1

## Prompt

> In tshell, use the scatter pipe |* to square each element in [1,2,3,4,5] in parallel, then reduce to sum them.

## Final Answer

```
55
```

## Attempts

### Attempt 1 (OK)

```javascript
[1,2,3,4,5] |* (x => x ** 2) |> reduce((a, x) => a + x, 0)
```

**Result:**
```
"55"
```

