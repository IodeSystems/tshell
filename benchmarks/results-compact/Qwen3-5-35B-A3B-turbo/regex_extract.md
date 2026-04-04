# regex_extract

**Status:** PASS
**Duration:** 4151ms
**Tool calls:** 1

## Prompt

> In tshell, extract all email-like patterns from the string "contact alice@example.com or bob@test.org for info". Use match() with a regex.

## Final Answer

```
["alice@example.com", "bob@test.org"]
```

## Attempts

### Attempt 1 (OK)

```javascript
let text = "contact alice@example.com or bob@test.org for info";
let pattern = /[\w.-]+@[\w.-]+\.[\w.-]+/g;
text.match(pattern)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

