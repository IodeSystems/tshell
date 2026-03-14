# regex_extract

**Status:** PASS
**Duration:** 9915ms
**Tool calls:** 2

## Prompt

> In tshell, extract all email-like patterns from the string "contact alice@example.com or bob@test.org for info". Use match() with a regex. Return the array of matches.

## Final Answer

```
The array of email matches is:

`["alice@example.com", "bob@test.org"]`
```

## Attempts

### Attempt 1 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" |> match(/[\w.+-]+@[\w.-]+\.[\w]+/g)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [], index: 8}, {match: \"bob@test.org\", groups: [], index: 29}]"
```

### Attempt 2 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" |> match(/[\w.+-]+@[\w.-]+\.[\w]+/g) |> map(m => m.match)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

