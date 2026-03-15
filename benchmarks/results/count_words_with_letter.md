# count_words_with_letter

**Status:** PASS
**Duration:** 4137ms
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
let s = r`the quick brown fox jumps over the lazy dog`;
let words = s.split(" ");
let count = words.filter(w => w.includes("o")).len();
count
```

**Result:**
```
"4"
```

