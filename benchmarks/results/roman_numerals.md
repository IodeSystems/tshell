# roman_numerals

**Status:** PASS
**Duration:** 10210ms
**Tool calls:** 1

## Prompt

> In tshell, write a function that converts an integer to a Roman numeral string. Convert 1994. Expected result: "MCMXCIV".

## Final Answer

```
MCMXCIV
```

## Attempts

### Attempt 1 (OK)

```javascript
const toRoman = (num: number): string => {
  const romanMap = [
    [1000, "M"],
    [900, "CM"],
    [500, "D"],
    [400, "CD"],
    [100, "C"],
    [90, "XC"],
    [50, "L"],
    [40, "XL"],
    [10, "X"],
    [9, "IX"],
    [5, "V"],
    [4, "IV"],
    [1, "I"]
  ];
  
  let result = "";
  let n = num;
  
  for (const [value, symbol] of romanMap) {
    while (n >= value) {
      result += symbol;
      n -= value;
    }
  }
  
  return result;
};

toRoman(1994);
```

**Result:**
```
"MCMXCIV"
```

