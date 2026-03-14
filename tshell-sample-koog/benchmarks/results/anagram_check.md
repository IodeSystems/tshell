# anagram_check

**Status:** PASS
**Duration:** 5967ms
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
function areAnagrams(word1, word2) {
  let normalize = w => w.toLowerCase().split('').sort().join('');
  return normalize(word1) === normalize(word2);
}

export function checkAnagrams(w1, w2) {
  return areAnagrams(w1, w2);
}

let result = checkAnagrams('listen', 'silent');
result
```

**Result:**
```
"true"
```

