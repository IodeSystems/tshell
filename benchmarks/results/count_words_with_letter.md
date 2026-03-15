# count_words_with_letter

**Status:** PASS
**Duration:** 3710ms
**Tool calls:** 1

## Prompt

> Using tshell, take the sentence 'the quick brown fox jumps over the lazy dog' and return the number of words that contain the letter 'o'.

## Final Answer

```
4
```

## Attempts

### Attempt 1 (OK)

```javascript
"the quick brown fox jumps over the lazy dog"
  |> split(" ")
  |> filter(w => w.contains("o"))
  |> len()
```

**Result:**
```
"4"
```

