# collatz_steps

**Status:** PASS
**Duration:** 5214ms
**Tool calls:** 1

## Prompt

> Using tshell, compute the number of steps in the Collatz sequence starting from 27 until it reaches 1. (If n is even, n/2; if odd, 3n+1).

## Final Answer

```
111
```

## Attempts

### Attempt 1 (OK)

```javascript
const collatz = (n) => n % 2 === 0 ? n / 2 : 3 * n + 1;

let steps = 0;
let n = 27;
while (n !== 1) {
  n = collatz(n);
  steps++;
}
steps
```

**Result:**
```
"111"
```

