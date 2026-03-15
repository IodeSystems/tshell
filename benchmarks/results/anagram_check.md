# anagram_check

**Status:** PASS
**Duration:** 4814ms
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
const isAnagram = (w1, w2) => {
  const normalize = (s) => s.toLowerCase().split("").sort().join("");
  return normalize(w1) === normalize(w2);
};

isAnagram("listen", "silent");
```

**Result:**
```
"true"
```

