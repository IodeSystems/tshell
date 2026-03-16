# regex_extract

**Status:** PASS
**Duration:** 4313ms
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
"contact alice@example.com or bob@test.org for info" |> match(/(\w+@\w+\.\w+)/g)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [\"alice@example.com\"], index: 8}, {match: \"bob@test.org\", groups: [\"bob@test.org\"], index: 29}]"
```

