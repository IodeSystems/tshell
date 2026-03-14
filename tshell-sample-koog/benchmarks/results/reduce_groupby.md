# reduce_groupby

**Status:** PASS
**Duration:** 14159ms
**Tool calls:** 1

## Prompt

> In tshell, given [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}], group by type into an object like {fruit:["apple","banana"],veg:["carrot","pea"]}. Use reduce.

## Final Answer

```
Perfect! Here's the solution that groups the array by `type` into an object with names as values:

```javascript
let data = [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}];

let result = data |> reduce((acc, item) => {
  let type = item.type;
  let name = item.name;
  if (!acc[type]) {
    acc[type] = [];
  }
  acc[type].push(name);
  return acc;
}, {});

result
```

Output: `{fruit: ["apple", "banana"], veg: ["carrot", "pea"]}`

The `reduce` function accumulates an object where:
- Keys are the `type` values ("fruit", "veg")
- Values are arrays of the corresponding `name` values
- The accumulator starts as an empty object `{}`
```

## Attempts

### Attempt 1 (OK)

```javascript
let data = [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}];

let result = data |> reduce((acc, item) => {
  let type = item.type;
  let name = item.name;
  if (!acc[type]) {
    acc[type] = [];
  }
  acc[type].push(name);
  return acc;
}, {});

result
```

**Result:**
```
"{fruit: [\"apple\", \"banana\"], veg: [\"carrot\", \"pea\"]}"
```

