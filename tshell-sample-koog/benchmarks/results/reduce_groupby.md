# reduce_groupby

**Status:** PASS
**Duration:** 8482ms
**Tool calls:** 1

## Prompt

> In tshell, given [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}], group by type into an object like {fruit:["apple","banana"],veg:["carrot","pea"]}. Use reduce.

## Final Answer

```
The result is `{fruit: ["apple", "banana"], veg: ["carrot", "pea"]}`. 

This uses `reduce` to accumulate items into an object where each key is a `type` and the value is an array of `name` values for that type.
```

## Attempts

### Attempt 1 (OK)

```javascript
let data = [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}];

data |> reduce((acc, item) => {
  let key = item.type;
  if (!acc[key]) {
    acc[key] = [];
  }
  acc[key].push(item.name);
  return acc;
}, {})
```

**Result:**
```
"{fruit: [\"apple\", \"banana\"], veg: [\"carrot\", \"pea\"]}"
```

