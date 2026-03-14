# reverse_words_order

**Status:** PASS
**Duration:** 5497ms
**Tool calls:** 1

## Prompt

> Using tshell, take the sentence 'the quick brown fox jumps over the lazy dog' and return the number of words that contain the letter 'o'.

## Final Answer

```
The sentence "the quick brown fox jumps over the lazy dog" contains **4** words that have the letter 'o':

1. brown
2. fox
3. over
4. dog
```

## Attempts

### Attempt 1 (OK)

```javascript
let sentence = 'the quick brown fox jumps over the lazy dog';
let words = sentence.split(' ');
let count = words |> filter(word => word.includes('o')) |> len();
count
```

**Result:**
```
"4"
```

