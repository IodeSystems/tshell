# reverse_words_order

**Status:** PASS
**Duration:** 4200ms
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
let sentence = 'the quick brown fox jumps over the lazy dog';
let words = sentence.split(' ');
words |> filter(word => word.includes('o')) |> len()
```

**Result:**
```
"4"
```

