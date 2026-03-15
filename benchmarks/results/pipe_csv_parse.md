# pipe_csv_parse

**Status:** PASS
**Duration:** 18626ms
**Tool calls:** 3

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
let lines = csv.lines();
let headers = lines[0].split(",");
let rows = lines.slice(1).map(row => {
  let vals = row.split(",");
  return headers.reduce((obj, h, i) => { obj[h] = vals[i]; return obj; }, {});
});
rows
```

**Result:**
```
"ERROR: Type mismatch in index\n\n  Expected: number\n  Got:      null (null)\n\n  at line 6:54"
```

### Attempt 2 (OK)

```javascript
let csv = "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la";
let lines = csv.lines();
let headers = lines[0].split(",");
let rows = lines.slice(1).map(row => {
  let vals = row.split(",");
  let obj = {};
  headers.forEach((h, i) => { obj[h] = vals[i]; });
  return obj;
});
rows
```

**Result:**
```
"ERROR: Type mismatch in index\n\n  Expected: number\n  Got:      null (null)\n\n  at line 7:43"
```

### Attempt 3 (OK)

```javascript
let csv = "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la";
let lines = csv.lines();
let headers = lines[0].split(",");
let rows = lines.slice(1).map(row => {
  let vals = row.split(",");
  let obj = {};
  for (let i = 0; i < headers.length; i++) {
    obj[headers[i]] = vals[i];
  }
  return obj;
});
rows
```

**Result:**
```
"[{name: \"alice\", age: \"30\", city: \"nyc\"}, {name: \"bob\", age: \"25\", city: \"sf\"}, {name: \"carol\", age: \"35\", city: \"la\"}]"
```

