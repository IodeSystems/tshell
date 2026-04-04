# reduce_groupby

**Status:** PASS
**Duration:** 12420ms
**Tool calls:** 2

## Prompt

> In tshell, given [{type:"fruit",name:"apple"},{type:"veg",name:"carrot"},{type:"fruit",name:"banana"},{type:"veg",name:"pea"}], group by type into an object like {fruit:[...],veg:[...]}. Use reduce.

## Final Answer

```
{fruit: [{type: "fruit", name: "apple"}, {type: "fruit", name: "banana"}], veg: [{type: "veg", name: "carrot"}, {type: "veg", name: "pea"}]}
```

## Attempts

### Attempt 1 (ERROR)

```javascript
let data = [{type:
```

**Result:**
```
"ERROR: Type mismatch in reduce

  Expected: array
  Got:      function (function <anonymous>(acc, item))

  Hint: Did you mean: array |> reduce(fn)? The first argument must be an array.

  at line 2:6"
```

### Attempt 2 (OK)

```javascript
let data = [{type:
```

**Result:**
```
"{fruit: [{type: "fruit", name: "apple"}, {type: "fruit", name: "banana"}], veg: [{type: "veg", name: "carrot"}, {type: "veg", name: "pea"}]}"
```

