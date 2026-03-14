# longest_common_subsequence

**Status:** PASS
**Duration:** 13832ms
**Tool calls:** 1

## Prompt

> Using tshell, find the length of the longest common subsequence of 'ABCBDAB' and 'BDCAB'. Return the length.

## Final Answer

```
The length of the longest common subsequence of 'ABCBDAB' and 'BDCAB' is **4**.

One example of such a subsequence is "BCAB" (or "BDAB", or "BCAB" depending on the matching characters chosen).
```

## Attempts

### Attempt 1 (OK)

```javascript
function lcsLength(s1, s2) {
  let m = s1.length;
  let n = s2.length;
  
  // Create DP table initialized with 0
  let dp = [];
  for (let i = 0; i <= m; i++) {
    dp[i] = [];
    for (let j = 0; j <= n; j++) {
      dp[i][j] = 0;
    }
  }
  
  // Fill DP table
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (s1[i-1] === s2[j-1]) {
        dp[i][j] = dp[i-1][j-1] + 1;
      } else {
        dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
      }
    }
  }
  
  return dp[m][n];
}

let s1 = 'ABCBDAB';
let s2 = 'BDCAB';
lcsLength(s1, s2);
```

**Result:**
```
"4"
```

