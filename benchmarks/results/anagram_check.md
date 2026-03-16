# anagram_check

**Status:** PASS
**Duration:** 5215ms
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
fn areAnagrams(word1, word2) {
  let sorted1 = word1.lower().split("").sort().join("")
  let sorted2 = word2.lower().split("").sort().join("")
  return sorted1 === sorted2
}

let result = areAnagrams("listen", "silent")
print(result)
```

**Result:**
```
"true"
```

