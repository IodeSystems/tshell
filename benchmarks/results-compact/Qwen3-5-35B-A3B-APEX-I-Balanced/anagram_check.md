# anagram_check

**Status:** PASS
**Duration:** 4850ms
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
  let lower1 = w1.lower().split('').sort().join('');
  let lower2 = w2.lower().split('').sort().join('');
  return lower1 === lower2;
}

isAnagram('listen', 'silent')
```

**Result:**
```
"true"
```

