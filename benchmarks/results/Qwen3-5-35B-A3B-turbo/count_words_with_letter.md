# count_words_with_letter

**Status:** PASS
**Duration:** 3741ms
**Tool calls:** 1

## Prompt

> Using tshell, take the sentence 'the quick brown fox jumps over the lazy dog' and count the number of words that contain the letter 'o'.

## Final Answer

```
4
```

## Attempts

### Attempt 1 (OK)

```javascript
let sentence = "the quick brown fox jumps over the lazy dog";
sentence.split(" ") |> filter(w => w.indexOf("o") >= 0) |> len()
```

**Result:**
```
"4"
```

