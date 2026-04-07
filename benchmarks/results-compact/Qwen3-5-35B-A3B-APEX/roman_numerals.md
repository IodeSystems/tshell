# roman_numerals

**Status:** PASS
**Duration:** 9514ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that converts an integer to a Roman numeral string. Convert 3749 and 2867, return them joined with a comma.

## Final Answer

```
MMMDCCXLIX,MMDCCCLXVII
```

## Attempts

### Attempt 1 (OK)

```javascript
function toRoman(num) {
  let values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
  let symbols = [
```

**Result:**
```
"MMMDCCXLIX,MMDCCCLXVII"
```

