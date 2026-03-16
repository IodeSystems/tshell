# regex_extract

**Status:** PASS
**Duration:** 12354ms
**Tool calls:** 3

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
"contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/) |> map(m => m.match)
```

**Result:**
```
"[\"alice@example.com\"]"
```

### Attempt 3 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g) |> map(m => m.match)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

