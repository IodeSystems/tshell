# roman_numerals

**Status:** PASS
**Duration:** 15630ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that converts an integer to a Roman numeral string. Convert 1994. Expected result: "MCMXCIV".

## Final Answer

```
The function successfully converts 1994 to the Roman numeral "MCMXCIV".

Here's how it works:
- It uses an array of value-symbol pairs ordered from largest to smallest (including subtractive combinations like 900="CM", 400="CD", 90="XC", 40="XL", 9="IX", 4="IV")
- For each value-symbol pair, it repeatedly appends the symbol and subtracts the value from the input number until the number is less than the current value
- This continues until the number is reduced to 0

The result for 1994 is correctly "MCMXCIV" (1000 + 900 + 90 + 4 = M + CM + XC + IV).
```

## Attempts

### Attempt 1 (OK)

```javascript
function toRoman(num) {
  let val = [
    [1000, 'M'],
    [900, 'CM'],
    [500, 'D'],
    [400, 'CD'],
    [100, 'C'],
    [90, 'XC'],
    [50, 'L'],
    [40, 'XL'],
    [10, 'X'],
    [9, 'IX'],
    [5, 'V'],
    [4, 'IV'],
    [1, 'I']
  ];
  
  let result = '';
  for (let [value, symbol] of val) {
    while (num >= value) {
      result += symbol;
      num -= value;
    }
  }
  return result;
}

let result = toRoman(1994);
result;
```

**Result:**
```
"MCMXCIV"
```

