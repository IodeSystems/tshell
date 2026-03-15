# reduce_groupby

**Status:** PASS
**Duration:** 6811ms
**Tool calls:** 1

## Prompt

> In tshell, given [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}], group by type into an object like {fruit:["apple","banana"],veg:["carrot","pea"]}. Use reduce.

## Final Answer

```
{fruit: ["apple", "banana"], veg: ["carrot", "pea"]}
```

## Attempts

### Attempt 1 (OK)

```javascript
[{"type":"fruit","name":"apple"},{"type":"veg","name":"carrot"},{"type":"fruit","name":"banana"},{"type":"veg","name":"pea"}]
|> reduce((acc, item) => {
  const t = item.type;
  if (!acc[t]) acc[t] = [];
  acc[t].push(item.name);
  return acc;
}, {})
```

**Result:**
```
"{fruit: [\"apple\", \"banana\"], veg: [\"carrot\", \"pea\"]}"
```

