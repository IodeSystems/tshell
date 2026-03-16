# roman_numerals

**Status:** PASS
**Duration:** 9226ms
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
fn toRoman(n) {
    const values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
    const numerals = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
    let result = "";
    for (let i = 0; i < values.length; i++) {
        while (n >= values[i]) {
            result += numerals[i];
            n -= values[i];
        }
    }
    return result;
}

toRoman(1994)
```

**Result:**
```
"MCMXCIV"
```

