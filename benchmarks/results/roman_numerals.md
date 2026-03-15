# roman_numerals

**Status:** PASS
**Duration:** 21035ms
**Tool calls:** 2

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
  let roman = {
    1000: "M",
    900: "CM",
    500: "D",
    400: "CD",
    100: "C",
    90: "XC",
    50: "L",
    40: "XL",
    10: "X",
    9: "IX",
    5: "V",
    4: "IV",
    1: "I"
  };
  
  let result = "";
  for (let key in Object.keys(roman).sort((a, b) => num - b)) {
    let k = num(key);
    while (num >= k) {
      result += roman[k];
      num -= k;
    }
  }
  
  return result;
}

intToRoman(1994);
```

**Result:**
```
"ERROR: Syntax error at line 3:4\n\n      1000: \"M\",\n      ^\n\n  Expected an identifier, got '1000'"
```

### Attempt 2 (OK)

```javascript
fn intToRoman(num) {
  let keys = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
  let romanMap = {
    "1000": "M",
    "900": "CM",
    "500": "D",
    "400": "CD",
    "100": "C",
    "90": "XC",
    "50": "L",
    "40": "XL",
    "10": "X",
    "9": "IX",
    "5": "V",
    "4": "IV",
    "1": "I"
  };
  
  let result = "";
  for (let i = 0; i < keys.length; i++) {
    let k = keys[i];
    while (num >= k) {
      result += romanMap[str(k)];
      num -= k;
    }
  }
  
  return result;
}

intToRoman(1994);
```

**Result:**
```
"MCMXCIV"
```

