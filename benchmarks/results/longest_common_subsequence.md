# longest_common_subsequence

**Status:** PASS
**Duration:** 9057ms
**Tool calls:** 1

## Prompt

> Using tshell, find the length of the longest common subsequence of 'ABCBDAB' and 'BDCAB'. Return the length.

## Final Answer

```
4
```

## Attempts

### Attempt 1 (OK)

```javascript
const s1 = "ABCBDAB";
const s2 = "BDCAB";

let n = s1.length;
let m = s2.length;

let dp = Array.from({length: n + 1}, () => Array(m + 1).fill(0));

for (let i = 1; i <= n; i++) {
  for (let j = 1; j <= m; j++) {
    if (s1[i - 1] === s2[j - 1]) {
      dp[i][j] = dp[i - 1][j - 1] + 1;
    } else {
      dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
    }
  }
}

dp[n][m];
```

**Result:**
```
"4"
```

