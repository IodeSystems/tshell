# anagram_check

**Status:** PASS
**Duration:** 3971ms
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
let isAnagram = (a, b) => a.lower().split('').sort().join('') == b.lower().split('').sort().join('');
isAnagram('listen', 'silent')
```

**Result:**
```
"true"
```

