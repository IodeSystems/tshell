# pipe_csv_parse

**Status:** PASS
**Duration:** 8813ms
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
let lines = csv.split("\n");
let headers = lines[0].split(",");
let data = lines.slice(1);
data.map(row => {
    let values = row.split(",");
    let obj = {};
    for (let i = 0; i < headers.length; i++) {
        obj[headers[i]] = values[i];
    }
    return obj;
});
```

**Result:**
```
"[{name: \"alice\", age: \"30\", city: \"nyc\"}, {name: \"bob\", age: \"25\", city: \"sf\"}, {name: \"carol\", age: \"35\", city: \"la\"}]"
```

