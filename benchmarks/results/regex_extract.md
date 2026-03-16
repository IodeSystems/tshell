# regex_extract

**Status:** PASS
**Duration:** 4382ms
**Tool calls:** 1

## Prompt

> In tshell, extract all email-like patterns from the string "contact alice@example.com or bob@test.org for info". Use match() with a regex. Return the array of matches.

## Final Answer

```
["alice@example.com", "bob@test.org"]
```

## Attempts

### Attempt 1 (OK)

```javascript
r"contact alice@example.com or bob@test.org for info" |> match(/[\w.-]+@[\w.-]+\.[a-z]+/gi)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

