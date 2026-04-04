# pipe_csv_parse

**Status:** PASS
**Duration:** 9078ms
**Tool calls:** 1

## Prompt

> In tshell, parse this CSV string into an array of objects: "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la". First row is headers.

## Final Answer

```
[{name: "alice", age: "30", city: "nyc"}, {name: "bob", age: "25", city: "sf"}, {name: "carol", age: "35", city: "la"}]
```

## Attempts

### Attempt 1 (OK)

```javascript
let csv = "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la";
let lines = csv.lines();
let headers = lines[0].split(",");
let rows = lines.slice(1).map(line => {
  let values = line.split(",");
  let obj = {};
  for (let i = 0; i < headers.length; i++) {
    obj[headers[i]] = values[i];
  }
  return obj;
});
rows
```

**Result:**
```
"[{name: \"alice\", age: \"30\", city: \"nyc\"}, {name: \"bob\", age: \"25\", city: \"sf\"}, {name: \"carol\", age: \"35\", city: \"la\"}]"
```

