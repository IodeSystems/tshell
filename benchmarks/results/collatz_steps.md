# collatz_steps

**Status:** PASS
**Duration:** 5584ms
**Tool calls:** 1

## Prompt

> Using tshell, compute the number of steps in the Collatz sequence starting from 27 until it reaches 1. (If n is even, n/2; if odd, 3n+1). Return the step count.

## Final Answer

```
111
```

## Attempts

### Attempt 1 (OK)

```javascript
let n = 27;
let steps = 0;
while (n !== 1) {
  if (n % 2 === 0) {
    n = n / 2;
  } else {
    n = 3 * n + 1;
  }
  steps++;
}
steps
```

**Result:**
```
"111"
```

