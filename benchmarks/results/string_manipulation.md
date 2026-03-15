# string_manipulation

**Status:** PASS
**Duration:** 4436ms
**Tool calls:** 1

## Prompt

> In tshell, take the string "hello world foo bar" and: split by spaces, reverse the array of words, uppercase each word, join with "-". Return the result.

## Final Answer

```
BAR-FOO-WORLD-HELLO
```

## Attempts

### Attempt 1 (OK)

```javascript
"hello world foo bar"
  |> split(" ")
  |> reverse()
  |> map(w => w.upper())
  |> join("-")
```

**Result:**
```
"BAR-FOO-WORLD-HELLO"
```

