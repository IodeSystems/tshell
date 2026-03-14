# anagram_check

**Status:** PASS
**Duration:** 10147ms
**Tool calls:** 1

## Prompt

> Using tshell, write a function that checks if two words are anagrams. Test it with 'listen' and 'silent'. Return true if they are anagrams, false otherwise.

## Final Answer

```
The function `areAnagrams` checks if two words are anagrams by:

1. Converting both words to lowercase
2. Splitting each word into an array of characters
3. Sorting the characters alphabetically
4. Joining them back into strings
5. Comparing if the sorted strings are equal

When tested with 'listen' and 'silent', it returns `true` because both words contain the same letters (l, i, s, t, e, n) just in a different order, making them anagrams.
```

## Attempts

### Attempt 1 (OK)

```javascript
function areAnagrams(word1, word2) {
  let normalize = w => w.toLowerCase().split('').sort().join('');
  return normalize(word1) === normalize(word2);
}

export function areAnagrams(word1, word2) {
  let normalize = w => w.toLowerCase().split('').sort().join('');
  return normalize(word1) === normalize(word2);
}

areAnagrams('listen', 'silent')
```

**Result:**
```
"true"
```

