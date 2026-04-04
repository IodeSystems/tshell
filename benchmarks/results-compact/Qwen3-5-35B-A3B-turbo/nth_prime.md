# nth_prime

**Status:** PASS
**Duration:** 5943ms
**Tool calls:** 1

## Prompt

> Using tshell, find the 50th prime number.

## Final Answer

```
229
```

## Attempts

### Attempt 1 (OK)

```javascript
let isPrime = n => {
  if (n < 2) return false;
  for (let i = 2; i * i <= n; i++) {
    if (n % i === 0) return false;
  }
  return true;
};

let primes = [];
let candidate = 2;
while (primes.len() < 50) {
  if (isPrime(candidate)) primes.push(candidate);
  candidate++;
}
primes.last()
```

**Result:**
```
"229"
```

