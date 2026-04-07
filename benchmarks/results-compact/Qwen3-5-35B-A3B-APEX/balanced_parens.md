# balanced_parens

**Status:** PASS
**Duration:** 6830ms
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
  let balance = 0;
  for (let c of s) {
    if (c === '(') balance++;
    else if (c === ')') balance--;
    if (balance < 0) return false;
  }
  return balance === 0;
};

let test1 = isBalanced('((())())');
let test2 = isBalanced('((()');
{test1, test2}
```

**Result:**
```
"{test1: true, test2: false}"
```

