# roman_numerals

**Status:** PASS
**Duration:** 15072ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that converts an integer to a Roman numeral string. Convert 1994. Expected result: "MCMXCIV".

## Final Answer

```
The function successfully converts 1994 to "MCMXCIV". Here's how it works:

1. **Mapping values to numerals**: Creates arrays of integer values and their corresponding Roman numeral representations, including subtractive combinations (like 900→"CM", 400→"CD", etc.)

2. **Iterative subtraction**: For each value in descending order, repeatedly subtract it from the input number and append the corresponding numeral until the value is no longer greater than or equal to the current number.

3. **Result**: 1994 → MCMXCIV (1000 + 900 + 90 + 4)

The function correctly handles all standard Roman numeral rules including the subtractive notation.
```

## Attempts

### Attempt 1 (OK)

```javascript
function intToRoman(num) {
  let values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
  let numerals = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
  let result = "";
  for (let i = 0; i < values.length; i++) {
    while (num >= values[i]) {
      result += numerals[i];
      num -= values[i];
    }
  }
  return result;
}

let result = intToRoman(1994);
result;
```

**Result:**
```
"MCMXCIV"
```

