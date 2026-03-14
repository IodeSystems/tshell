# regex_extract

**Status:** PASS
**Duration:** 17091ms
**Tool calls:** 4

## Prompt

> In tshell, extract all email-like patterns from the string "contact alice@example.com or bob@test.org for info". Use match() with a regex. Return the array of matches.

## Final Answer

```
["alice@example.com", "bob@test.org"]
```

## Attempts

### Attempt 1 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [\"alice@example.com\"], index: 8}]"
```

### Attempt 2 (OK)

```javascript
let matches = "contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/);
matches |> map(m => m.match)
```

**Result:**
```
"[\"alice@example.com\"]"
```

### Attempt 3 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [\"alice@example.com\"], index: 8}, {match: \"bob@test.org\", groups: [\"bob@test.org\"], index: 29}]"
```

### Attempt 4 (OK)

```javascript
let matches = "contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g);
matches |> map(m => m.match)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

