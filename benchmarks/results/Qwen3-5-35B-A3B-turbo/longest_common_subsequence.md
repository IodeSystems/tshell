# longest_common_subsequence

**Status:** PASS
**Duration:** 7791ms
**Tool calls:** 1

## Prompt

> Using tshell, find the length of the longest common subsequence of 'ABCBDAB' and 'BDCAB'.

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
let dp = Array(m + 1).fill(0).map(() => Array(n + 1).fill(0));
for (let i = 1; i <= m; i++) {
  for (let j = 1; j <= n; j++) {
    if (s1[i - 1] === s2[j - 1]) {
      dp[i][j] = dp[i - 1][j - 1] + 1;
    } else {
      dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
    }
  }
}
dp[m][n];
```

**Result:**
```
"4"
```

