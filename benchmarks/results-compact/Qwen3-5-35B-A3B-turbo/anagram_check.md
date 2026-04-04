# anagram_check

**Status:** PASS
**Duration:** 4119ms
**Tool calls:** 1

## Prompt

> Using tshell, write a function that checks if two words are anagrams. Test it with 'listen' and 'silent'.

## Final Answer

```
true
```

## Attempts

### Attempt 1 (OK)

```javascript
function isAnagram(w1, w2) {
  let sortWord = (w) => w.toLowerCase().split("").sort().join("");
  return sortWord(w1) == sortWord(w2);
}
isAnagram("listen", "silent")
```

**Result:**
```
"true"
```

