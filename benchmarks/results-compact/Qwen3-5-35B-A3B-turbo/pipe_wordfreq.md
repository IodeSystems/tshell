# pipe_wordfreq

**Status:** PASS
**Duration:** 13114ms
**Tool calls:** 4

## Prompt

> In tshell, take the string "the cat sat on the mat the cat" and use pipes to: split by spaces, count word frequencies into an object.

## Final Answer

```
{"the": 3, "cat": 2, "sat": 1, "on": 1, "mat": 1}
```

## Attempts

### Attempt 1 (ERROR)

```javascript
"the cat sat on the mat the cat" |> split(" ") |> countBy()
```

**Result:**
```
"ERROR: Wrong arguments for 'countBy'\n\n  Expected: countBy(function)\n  Got:      countBy(array: [\"the\", \"cat\", \"sat\", \"on\", \"the\", \"mat\", \"the\", \"cat\"])\n\n  Example:\n    countBy(x => x.field)\n\n  at line 1:0"
```

### Attempt 2 (OK)

```javascript
"the cat sat on the mat the cat" |> split(" ") |> groupBy(x => x) |> map((k, v) => [k, len(v)]) |> fromEntries()
```

**Result:**
```
"{the: [[\"the\", \"the\", \"the\"], 3], cat: [[\"cat\", \"cat\"], 3], sat: [[\"sat\"], 3], on: [[\"on\"], 2], mat: [[\"mat\"], 3]}"
```

### Attempt 3 (OK)

```javascript
"the cat sat on the mat the cat" |> split(" ") |> groupBy(x => x) |> map((k, v) => [k, len(v)]) |> fromEntries()
```

**Result:**
```
"{the: [[\"the\", \"the\", \"the\"], 3], cat: [[\"cat\", \"cat\"], 3], sat: [[\"sat\"], 3], on: [[\"on\"], 2], mat: [[\"mat\"], 3]}"
```

### Attempt 4 (OK)

```javascript
"the cat sat on the mat the cat" |> split(" ") |> groupBy(x => x) |> map(v => v |> len()) |> fromEntries()
```

**Result:**
```
"{the: 3, cat: 2, sat: 1, on: 1, mat: 1}"
```

