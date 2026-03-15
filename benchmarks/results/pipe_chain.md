# pipe_chain

**Status:** PASS
**Duration:** 4899ms
**Tool calls:** 1

## Prompt

> In tshell, take the array [5,3,8,1,9,2,7,4,6] and use pipes to: sort it, take the top 3, double each, then sum them. Return the sum.

## Final Answer

```
48
```

## Attempts

### Attempt 1 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort("desc") |> limit(3) |> map(n => n * 2) |> reduce((a, n) => a + n, 0)
```

**Result:**
```
"48"
```

