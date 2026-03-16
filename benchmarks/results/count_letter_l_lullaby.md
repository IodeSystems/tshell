# count_letter_l_lullaby

**Status:** PASS
**Duration:** 3321ms
**Tool calls:** 1

## Prompt

> Using tshell, count the number of times the letter 'l' appears in the word 'lullaby'. Return the count as a number.

## Final Answer

```
3
```

## Attempts

### Attempt 1 (OK)

```javascript
"lullaby".match(/l/g) |> len()
```

**Result:**
```
"3"
```

