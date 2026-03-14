# regex_extract

**Status:** PASS
**Duration:** 17756ms
**Tool calls:** 3

## Prompt

> In tshell, extract all email-like patterns from the string "contact alice@example.com or bob@test.org for info". Use match() with a regex. Return the array of matches.

## Final Answer

```
The array of email-like patterns extracted from the string is:

```
["alice@example.com", "bob@test.org"]
```

I used the `match()` function with a global regex flag `/g` to find all email patterns, then mapped over the results to extract just the `match` field from each match object, giving us a clean array of email addresses.
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
"contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g)
```

**Result:**
```
"[{match: \"alice@example.com\", groups: [\"alice@example.com\"], index: 8}, {match: \"bob@test.org\", groups: [\"bob@test.org\"], index: 29}]"
```

### Attempt 3 (OK)

```javascript
"contact alice@example.com or bob@test.org for info" |> match(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g) |> map(m => m.match)
```

**Result:**
```
"[\"alice@example.com\", \"bob@test.org\"]"
```

