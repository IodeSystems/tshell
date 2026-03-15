# nth_prime

**Status:** PASS
**Duration:** 8042ms
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
let isPrime = (n) => {
  if (n < 2) return false;
  if (n === 2) return true;
  if (n % 2 === 0) return false;
  for (let i = 3; i * i <= n; i += 2) {
    if (n % i === 0) return false;
  }
  return true;
};

let count = 0;
let num = 2;
while (count < 50) {
  if (isPrime(num)) {
    count++;
  }
  if (count < 50) {
    num++;
  }
}
num
```

**Result:**
```
"229"
```

