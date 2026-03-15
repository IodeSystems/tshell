# pipe_csv_parse

**Status:** PASS
**Duration:** 9691ms
**Tool calls:** 1

## Prompt

> In tshell, parse this CSV string into an array of objects: "name,age,city\nalice,30,nyc\nbob,25,sf\ncarol,35,la". First row is headers. Return the array of objects.

## Final Answer

```
[{name: "alice", age: "30", city: "nyc"}, {name: "bob", age: "25", city: "sf"}, {name: "carol", age: "35", city: "la"}]
```

## Attempts

### Attempt 1 (OK)

```javascript
let csv = r`name,age,city
alice,30,nyc
bob,25,sf
carol,35,la`;

let lines = csv.trim().split('\n');
let headers = lines[0].split(',');
let objects = [];
for (let i = 1; i < lines.length; i++) {
  let values = lines[i].split(',');
  let obj = {};
  for (let j = 0; j < headers.length; j++) {
    obj[headers[j]] = values[j];
  }
  objects.push(obj);
}
objects
```

**Result:**
```
"[{name: \"alice\", age: \"30\", city: \"nyc\"}, {name: \"bob\", age: \"25\", city: \"sf\"}, {name: \"carol\", age: \"35\", city: \"la\"}]"
```

