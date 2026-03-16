# roman_numerals

**Status:** PASS
**Duration:** 9207ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that converts an integer to a Roman numeral string. Convert 1994. Expected result: "MCMXCIV".

## Final Answer

```
"MCMXCIV"
```

## Attempts

### Attempt 1 (OK)

```javascript
fn intToRoman(num) {
  let values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
  let symbols = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
  let result = "";
  for (let i = 0; i < values.len(); i++) {
    while (num >= values[i]) {
      num -= values[i];
      result += symbols[i];
    }
  }
  return result;
}

intToRoman(1994)
```

**Result:**
```
"MCMXCIV"
```

