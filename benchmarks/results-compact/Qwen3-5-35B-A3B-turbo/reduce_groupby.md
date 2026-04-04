# reduce_groupby

**Status:** PASS
**Duration:** 7316ms
**Tool calls:** 1

## Prompt

> In tshell, given [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}], group by type into an object like {fruit:[...],veg:[...]}. Use reduce.

## Final Answer

```
{fruit: [{type: "fruit", name: "apple"}, {type: "fruit", name: "banana"}], veg: [{type: "veg", name: "carrot"}, {type: "veg", name: "pea"}]}
```

## Attempts

### Attempt 1 (OK)

```javascript
[{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}]
|> reduce((acc, item) => {
  if (!acc[item.type]) acc[item.type] = [];
  acc[item.type].push(item);
  return acc;
}, {})
```

**Result:**
```
"{fruit: [{type: \"fruit\", name: \"apple\"}, {type: \"fruit\", name: \"banana\"}], veg: [{type: \"veg\", name: \"carrot\"}, {type: \"veg\", name: \"pea\"}]}"
```

