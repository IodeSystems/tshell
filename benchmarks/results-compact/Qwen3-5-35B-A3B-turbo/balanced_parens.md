# balanced_parens

**Status:** PASS
**Duration:** 7158ms
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
function isBalanced(str) {
    let count = 0;
    for (let i = 0; i < len(str); i++) {
        let ch = charAt(str, i);
        if (ch == '(') count++;
        else if (ch == ')') {
            count--;
            if (count < 0) return false;
        }
    }
    return count == 0;
}

let test1 = isBalanced('((())())');
let test2 = isBalanced('((()');

{test1, test2};
```

**Result:**
```
"{test1: true, test2: false}"
```

