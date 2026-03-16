# anagram_check

**Status:** PASS
**Duration:** 5217ms
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
fn isAnagram(word1: string, word2: string) {
  let normalize = (w: string) => w.toLowerCase().split("").sort().join("");
  return normalize(word1) === normalize(word2);
}

let result = isAnagram("listen", "silent");
print(result)
```

**Result:**
```
"true"
```

