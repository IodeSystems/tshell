# nth_prime

**Status:** PASS
**Duration:** 13498ms
**Tool calls:** 2

## Prompt

> Using tshell, find the 50th prime number.

## Final Answer

```
229
```

## Attempts

### Attempt 1 (ERROR)

```javascript
fn isPrime(n) {
  if (n < 2) return false;
  if (n === 2) return true;
  if (n % 2 === 0) return false;
  for (let i = 3; i * i <= n; i += 2) {
    if (n % i === 0) return false;
  }
  return true;
}

let primes = [];
let num = 2;
while (primes.len < 50) {
  if (isPrime(num)) primes.push(num);
  num++;
}

primes[49]
```

**Result:**
```
"ERROR: Type mismatch in comparison

  Expected: matching number or string types
  Got:      function (function len())

  Hint: Did you mean .length (property) or len(x) (function call)? .len is a function — use .length or len(x) instead"
```

### Attempt 2 (OK)

```javascript
fn isPrime(n) {
  if (n < 2) return false;
  if (n === 2) return true;
  if (n % 2 === 0) return false;
  for (let i = 3; i * i <= n; i += 2) {
    if (n % i === 0) return false;
  }
  return true;
}

let primes = [];
let num = 2;
while (primes.length < 50) {
  if (isPrime(num)) primes.push(num);
  num++;
}

primes[49]
```

**Result:**
```
"229"
```

