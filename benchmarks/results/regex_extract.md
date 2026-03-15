# regex_extract

**Status:** PASS
**Duration:** 21421ms
**Tool calls:** 6

## Prompt

> In tshell, extract all email-like patterns from the string "contact alice@example.com or bob@test.org for info". Use match() with a regex. Return the array of matches.

## Final Answer

```
["alice@example.com", "bob@test.org"]
```

## Attempts

### Attempt 1 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" | match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/)
```

**Result:**
```
"ERROR: Type mismatch in pipe into match\n\n  Expected: string\n  Got:      regex (/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})/)\n\n  at line 1:60"
```

### Attempt 2 (OK)

```javascript
match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/, "contact alice@example.com or bob@test.org for info")
```

**Result:**
```
"ERROR: Type mismatch in pipe into match\n\n  Expected: string\n  Got:      regex (/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})/)\n\n  at line 1:5"
```

### Attempt 3 (OK)

```javascript
match("contact alice@example.com or bob@test.org for info", /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [\"alice@example.com\"], index: 8}]"
```

### Attempt 4 (OK)

```javascript
match("contact alice@example.com or bob@test.org for info", /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/) |> map(r => r.match)
```

**Result:**
```
"[\"alice@example.com\"]"
```

### Attempt 5 (OK)

```javascript
match("contact alice@example.com or bob@test.org for info", /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [\"alice@example.com\"], index: 8}, {match: \"bob@test.org\", groups: [\"bob@test.org\"], index: 29}]"
```

### Attempt 6 (OK)

```javascript
match("contact alice@example.com or bob@test.org for info", /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g) |> map(r => r.match)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

