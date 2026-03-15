# object_transform

**Status:** PASS
**Duration:** 5269ms
**Tool calls:** 1

## Prompt

> In tshell, given the array [{name:"alice",score:85},{name:"bob",score:92},{name:"carol",score:78}], use pipes to filter scores > 80, extract names, sort them, and join with commas.

## Final Answer

```
"alice, bob"
```

## Attempts

### Attempt 1 (OK)

```javascript
[{name:"alice",score:85},{name:"bob",score:92},{name:"carol",score:78}]
|> filter(s => s.score > 80)
|> map(s => s.name)
|> sort()
|> join(", ")
```

**Result:**
```
"alice, bob"
```

