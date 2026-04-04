# factorial

**Status:** PASS
**Duration:** 27380ms
**Tool calls:** 2

## Prompt

> Compute 7 factorial (7!) using tshell.

## Final Answer

```
5040
```

## Attempts

### Attempt 1 (ERROR)

```javascript
range(1, 8) | reduce((a, x) => a * x, 1)
```

**Result:**
```
"ERROR: '|' is not supported. Did you mean:
  |>   pipe        (value |> function)
  |*   scatter     (array |* function)
  ||   logical OR  (a || b)
  |:   bitwise OR  (5 |: 3 → 7)"
```

### Attempt 2 (OK)

```javascript
range(1, 8) |> reduce((a, x) => a * x, 1)
```

**Result:**
```
"5040"
```

