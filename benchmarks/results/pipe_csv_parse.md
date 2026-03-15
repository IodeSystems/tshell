# pipe_csv_parse

**Status:** PASS
**Duration:** 14445ms
**Tool calls:** 2

## Prompt

> In tshell, parse this CSV string into an array of objects: "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la". First row is headers. Return the array of objects.

## Final Answer

```
[{name: "alice", age: "30", city: "nyc"}, {name: "bob", age: "25", city: "sf"}, {name: "carol", age: "35", city: "la"}]
```

## Attempts

### Attempt 1 (OK)

```javascript
let csv = "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la";
let lines = csv.split("\n");
let headers = lines[0].split(",");
let data = lines.slice(1).map(line => {
  let values = line.split(",");
  return headers.reduce((obj, h, i) => {
    obj[h] = values[i];
    return obj;
  }, {});
});
data
```

**Result:**
```
"ERROR: Type mismatch in index\n\n  Expected: number\n  Got:      null (null)\n\n  at line 7:19"
```

### Attempt 2 (OK)

```javascript
let csv = "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la";
let lines = csv.split("\n");
let headers = lines[0].split(",");
let data = lines.slice(1).map(line => {
  let values = line.split(",");
  let obj = {};
  for (let i = 0; i < headers.length; i++) {
    obj[headers[i]] = values[i];
  }
  return obj;
});
data
```

**Result:**
```
"[{name: \"alice\", age: \"30\", city: \"nyc\"}, {name: \"bob\", age: \"25\", city: \"sf\"}, {name: \"carol\", age: \"35\", city: \"la\"}]"
```

