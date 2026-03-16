# anagram_check

**Status:** PASS
**Duration:** 4743ms
**Tool calls:** 1

## Prompt

> Using tshell, write a function that checks if two words are anagrams. Test it with 'listen' and 'silent'. Return true if they are anagrams, false otherwise.

## Final Answer

```
true
```

## Attempts

### Attempt 1 (OK)

```javascript
let isAnagram = (word1, word2) => {
  let normalize = word => word.toLowerCase().split('').sort().join('');
  return normalize(word1) === normalize(word2);
};

isAnagram('listen', 'silent');
```

**Result:**
```
"true"
```

