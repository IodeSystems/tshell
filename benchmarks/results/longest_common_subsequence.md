# longest_common_subsequence

**Status:** PASS
**Duration:** 18310ms
**Tool calls:** 2

## Prompt

> Using tshell, find the length of the longest common subsequence of 'ABCBDAB' and 'BDCAB'. Return the length.

## Final Answer

```
4
```

## Attempts

### Attempt 1 (OK)

```javascript
let s1 = 'ABCBDAB';
let s2 = 'BDCAB';
let m = s1.len();
let n = s2.len();
let dp = Array.range(m + 1).map(i => Array.range(n + 1).fill(0));

for (let i = 1; i <= m; i++) {
  for (let j = 1; j <= n; j++) {
    if (s1.charAt(i - 1) === s2.charAt(j - 1)) {
      dp[i][j] = 1 + dp[i - 1][j - 1];
    } else {
      dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
    }
  }
}

dp[m][n];
```

**Result:**
```
"ERROR: Type mismatch in call\n\n  Expected: function\n  Got:      null (null)\n\n  at line 5:20"
```

### Attempt 2 (OK)

```javascript
let s1 = 'ABCBDAB';
let s2 = 'BDCAB';
let m = s1.len();
let n = s2.len();
let dp = [];
for (let i = 0; i <= m; i++) {
  let row = [];
  for (let j = 0; j <= n; j++) {
    row.push(0);
  }
  dp.push(row);
}

for (let i = 1; i <= m; i++) {
  for (let j = 1; j <= n; j++) {
    if (s1.charAt(i - 1) === s2.charAt(j - 1)) {
      dp[i][j] = 1 + dp[i - 1][j - 1];
    } else {
      let a = dp[i - 1][j];
      let b = dp[i][j - 1];
      dp[i][j] = a > b ? a : b;
    }
  }
}

dp[m][n];
```

**Result:**
```
"4"
```

