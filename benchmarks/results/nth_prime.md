# nth_prime

**Status:** PASS
**Duration:** 7323ms
**Tool calls:** 1

## Prompt

> Using tshell, find the 50th prime number. Return just the number.

## Final Answer

```
229
```

## Attempts

### Attempt 1 (OK)

```javascript
let isPrime = n => {
  if (n < 2) return false;
  if (n === 2) return true;
  if (n % 2 === 0) return false;
  for (let i = 3; i * i <= n; i += 2) {
    if (n % i === 0) return false;
  }
  return true;
};

let count = 0;
let current = 1;
while (count < 50) {
  current++;
  if (isPrime(current)) count++;
}
current
```

**Result:**
```
"229"
```

