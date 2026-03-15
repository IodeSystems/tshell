# balanced_parens

**Status:** PASS
**Duration:** 6639ms
**Tool calls:** 1

## Prompt

> Using tshell, write a function that checks if a string of parentheses is balanced. Test with '((())())' and '((()'. Return an object {test1: true/false, test2: true/false}.

## Final Answer

```
{test1: true, test2: false}
```

## Attempts

### Attempt 1 (OK)

```javascript
let isBalanced = (s) => {
  let count = 0;
  for (let c of s) {
    if (c === '(') count++;
    else if (c === ')') {
      count--;
      if (count < 0) return false;
    }
  }
  return count === 0;
};

{test1: isBalanced('((())())'), test2: isBalanced('((()')}
```

**Result:**
```
"{test1: true, test2: false}"
```

