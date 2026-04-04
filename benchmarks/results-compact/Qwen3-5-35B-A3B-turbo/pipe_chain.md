# pipe_chain

**Status:** PASS
**Duration:** 4938ms
**Tool calls:** 1

## Prompt

> In tshell, take the array [5,3,8,1,9,2,7,4,6] and use pipes (|>) to: sort it, take the top 3, double each, then sum them.

## Final Answer

```
48
```

## Attempts

### Attempt 1 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort() |> reverse() |> slice(0, 3) |> map(x => x * 2) |> reduce((a, x) => a + x, 0)
```

**Result:**
```
"48"
```

