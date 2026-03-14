# pipe_chain

**Status:** FAIL
**Duration:** 30005ms
**Tool calls:** 9
**Error:** TIMEOUT (30s)

## Prompt

> In tshell, take the array [5,3,8,1,9,2,7,4,6] and use pipes to: sort it, take the top 3, double each, then sum them. Return the sum.

## Final Answer

```

```

## Attempts

### Attempt 1 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort("desc") |> limit(3) |> map(n => n * 2) |> reduce((acc, n) => acc + n, 0)
```

**Result:**
```
"12"
```

### Attempt 2 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort("desc")
```

**Result:**
```
"[1, 2, 3, 4, 5, 6, 7, 8, 9]"
```

### Attempt 3 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort((a, b) => b - a)
```

**Result:**
```
"[1, 2, 3, 4, 5, 6, 7, 8, 9]"
```

### Attempt 4 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort("asc")
```

**Result:**
```
"[1, 2, 3, 4, 5, 6, 7, 8, 9]"
```

### Attempt 5 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> sort
```

**Result:**
```
"[1, 2, 3, 4, 5, 6, 7, 8, 9]"
```

### Attempt 6 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> reverse |> sort
```

**Result:**
```
"[1, 2, 3, 4, 5, 6, 7, 8, 9]"
```

### Attempt 7 (OK)

```javascript
help("sort")
```

**Result:**
```
"sort(input: array, key?: string)\n\n  sorts; optional key for objects\n\n  Examples:\n    [3, 1, 2] |> sort()\n    sort([3, 1, 2])\n    users |> sort(\"name\")"
```

### Attempt 8 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> map(n => -n) |> sort |> map(n => -n)
```

**Result:**
```
"[9, 8, 7, 6, 5, 4, 3, 2, 1]"
```

### Attempt 9 (OK)

```javascript
[5,3,8,1,9,2,7,4,6] |> map(n => -n) |> sort |> map(n => -n) |> limit(3) |> map(n => n * 2) |> reduce((acc, n) => acc + n, 0)
```

**Result:**
```
"48"
```

