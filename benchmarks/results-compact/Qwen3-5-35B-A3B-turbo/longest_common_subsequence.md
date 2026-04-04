# longest_common_subsequence

**Status:** PASS
**Duration:** 8924ms
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
function lcs(a, b) {
    let m = len(a), n = len(b);
    let dp = [];
    for (let i = 0; i <= m; i++) {
        dp[i] = [];
        for (let j = 0; j <= n; j++) {
            dp[i][j] = 0;
        }
    }
    for (let i = 1; i <= m; i++) {
        for (let j = 1; j <= n; j++) {
            if (a[i-1] == b[j-1]) {
                dp[i][j] = 1 + dp[i-1][j-1];
            } else {
                dp[i][j] = max(dp[i-1][j], dp[i][j-1]);
            }
        }
    }
    return dp[m][n];
}

lcs("ABCBDAB", "BDCAB")
```

**Result:**
```
"4"
```

