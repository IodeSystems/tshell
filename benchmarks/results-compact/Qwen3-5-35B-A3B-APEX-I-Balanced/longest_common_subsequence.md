# longest_common_subsequence

**Status:** PASS
**Duration:** 10382ms
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
function lcs(s1, s2) {
  let m = len(s1);
  let n = len(s2);
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
      if (charAt(s1, i - 1) === charAt(s2, j - 1)) {
        dp[i][j] = dp[i - 1][j - 1] + 1;
      } else {
        dp[i][j] = max(dp[i - 1][j], dp[i][j - 1]);
      }
    }
  }
  return dp[m][n];
}

lcs(
```

**Result:**
```
"4"
```

