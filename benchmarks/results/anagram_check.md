# anagram_check

**Status:** PASS
**Duration:** 4783ms
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
let isAnagram = (w1, w2) => {
  let sorted = (word) => word.lower().split("").sort().join("");
  return sorted(w1) == sorted(w2);
};

print(isAnagram("listen", "silent"));
```

**Result:**
```
"true"
```

