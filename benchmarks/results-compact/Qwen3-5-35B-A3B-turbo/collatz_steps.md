# collatz_steps

**Status:** PASS
**Duration:** 5922ms
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
let collatzSteps = (n) => {
  let steps = 0;
  while (n !== 1) {
    if (n % 2 === 0) {
      n = n / 2;
    } else {
      n = 3 * n + 1;
    }
    steps++;
  }
  return steps;
};
collatzSteps(27)
```

**Result:**
```
"111"
```

