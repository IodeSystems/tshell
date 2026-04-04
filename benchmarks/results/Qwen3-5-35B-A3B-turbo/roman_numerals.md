# roman_numerals

**Status:** PASS
**Duration:** 15000ms
**Tool calls:** 2

## Prompt

> In tshell, write a function that converts an integer to a Roman numeral string. Convert 1994.

## Final Answer

```
"MCMXCIV"
```

## Attempts

### Attempt 1 (ERROR)

```javascript
function toRoman(n) {
    let values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
    let symbols = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
    let result = "";
    for (let i = 0; i < values.length; i++) {
        while (n >= values[i]) {
            result += symbols[i];
            n -= values[i];
        }
    }
    return result;
}
letroman(1994);
```

**Result:**
```
"ERROR: Unknown command 'letroman'\n\n  Available commands:\n    Array\n    Boolean\n    JSON\n    Math\n    Number\n    Object\n    Promise\n    String\n    abs\n    all\n    any\n    assert\n    at\n    bool\n    ceil\n    chain\n    charAt\n    charCodeAt\n    chars\n    chunk\n    codePointAt\n    columns\n    concat\n    console\n    contains\n    countBy\n    difference\n    endsWith\n    entries\n    every\n    extendLimit\n    fail\n    fill\n    filter\n    find\n    first\n    flat\n  …
```

### Attempt 2 (OK)

```javascript
function toRoman(n) {
    let values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
    let symbols = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
    let result = "";
    for (let i = 0; i < values.length; i++) {
        while (n >= values[i]) {
            result += symbols[i];
            n -= values[i];
        }
    }
    return result;
}
toRoman(1994);
```

**Result:**
```
"MCMXCIV"
```

