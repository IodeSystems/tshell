# reduce_groupby

**Status:** PASS
**Duration:** 7116ms
**Tool calls:** 1

## Prompt

> In tshell, given [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}], group by type into an object like {fruit:["apple","banana"],veg:["carrot","pea"]}. Use reduce.

## Final Answer

```
{fruit:["apple","banana"],veg:["carrot","pea"]}
```

## Attempts

### Attempt 1 (OK)

```javascript
let data = [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}];
data |> reduce((acc, item) => {
  let type = item.type;
  if (!acc[type]) {
    acc[type] = [];
  }
  acc[type].push(item.name);
  return acc;
}, {})
```

**Result:**
```
"{fruit: [\"apple\", \"banana\"], veg: [\"carrot\", \"pea\"]}"
```

