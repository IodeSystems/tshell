# GraphToolkit

Property graph with typed nodes, typed edges, optional schema enforcement, and pipe-based traversal. Part of the `tshell` core module.

## Setup

```kotlin
val store = InMemoryGraphStore()
val schema = GraphSchema(
  nodes = mapOf("person" to NodeSchema(required = setOf("name"), optional = setOf("age"))),
  edges = mapOf("worksAt" to EdgeSchema(from = "person", to = "company")),
  strict = false  // true = reject unknown node/edge types
)
GraphToolkit(store, schema).install(shell)
```

Schema is optional. Without it, any node type, edge type, and properties are allowed.

## Commands

### Query start points

| Command | Description |
|---------|-------------|
| `root()` | Returns the graph root node. All nodes connect to root via `addNode`. |
| `node(id)` | Gets a node by id. |
| `nodes(type?)` | Gets all nodes, optionally filtered by type. |

### CRUD

| Command | Description |
|---------|-------------|
| `addNode(parent, type, props?)` | Creates a node connected from parent. |
| `link(from, to, type?, props?)` | Creates an edge between two nodes. |
| `unlink(edge)` | Removes an edge. |
| `removeNode(node)` | Removes a node and all its edges. |
| `setProps(target, props)` | Merges properties into a node or edge. |

### Traversal (pipe-compatible)

| Command | Description |
|---------|-------------|
| `out(node, type?)` | Follows outgoing edges, returns target nodes. |
| `inbound(node, type?)` | Follows incoming edges, returns source nodes. |
| `both(node, type?)` | Follows edges in both directions. |
| `outE(node, type?)` | Gets outgoing edges as objects. |
| `inE(node, type?)` | Gets incoming edges as objects. |

All traversal commands accept a single node or an array, and return an array. This means `filter()`, `map()`, `reduce()`, `sort()` compose naturally with them.

## Examples

### Build a graph

```javascript
let alice = addNode(root(), "person", {name: "Alice", age: 30})
let bob = addNode(root(), "person", {name: "Bob", age: 25})
let acme = addNode(root(), "company", {name: "Acme"})
link(alice, acme, "worksAt")
link(bob, acme, "worksAt")
link(alice, bob, "knows", {since: 2020})
```

### Traverse and query

```javascript
// All people
root() |> out("person")

// Filtered
root() |> out("person") |> filter(n => n.age > 28)

// Sorted names
root() |> out("person") |> map(n => n.name) |> sort()

// Multi-hop: root → person → company
root() |> out("person") |> out("worksAt") |> map(c => c.name)

// Reverse: who works at Acme?
nodes("company") |> filter(c => c.name == "Acme") |> inbound("worksAt") |> map(n => n.name)
```

### Edge inspection

```javascript
node(alice.id) |> outE("knows")
// → [{id: "e3", from: "n2", to: "n3", type: "knows", since: 2020}]

node(alice.id) |> outE() |> filter(e => e.type == "worksAt")
```

### Analytics

```javascript
// Degree count per person
nodes("person") |> map(p => {
  name: p.name,
  connections: (node(p.id) |> outE() |> len())
}) |> sort("connections") |> reverse()

// Group employees by company
nodes("company") |> map(c => {
  company: c.name,
  employees: (node(c.id) |> inbound("worksAt") |> map(e => e.name))
})
```

## Schema enforcement

```kotlin
GraphSchema(
  nodes = mapOf(
    "person" to NodeSchema(
      required = setOf("name"),     // must be present
      optional = setOf("age")       // null = any extra props allowed
    )
  ),
  edges = mapOf(
    "worksAt" to EdgeSchema(
      from = "person",              // source node type constraint
      to = "company"                // target node type constraint
    )
  ),
  strict = true  // reject unknown node/edge types entirely
)
```

- **Required properties**: `addNode` fails if missing.
- **Optional whitelist**: When set, `addNode` and `setProps` reject unknown properties. When `null`, any extra properties are allowed.
- **Edge type constraints**: `link` validates source and target node types.
- **Strict mode**: Rejects node types and edge types not listed in the schema.

Schema rules appear in the LLM's `help("graph")` output automatically.

## Traversal limits

```kotlin
GraphToolkit(store, maxTraversals = 10_000)
```

Each traversal step (`out`, `inbound`, `both`, `outE`, `inE`) counts edges visited. The counter resets on each `root()` or `node()` call. Exceeding the limit throws an error suggesting a more specific edge type filter.

## Custom GraphStore

`InMemoryGraphStore` is provided for testing and small graphs. Implement the `GraphStore` interface for persistent storage:

```kotlin
interface GraphStore {
  val rootId: String
  fun addNode(type: String, properties: Map<String, TShellValue>): GraphNode
  fun addEdge(fromId: String, toId: String, type: String?, properties: Map<String, TShellValue>): GraphEdge
  fun getNode(id: String): GraphNode?
  fun getEdge(id: String): GraphEdge?
  fun removeNode(id: String)
  fun removeEdge(id: String)
  fun outgoing(nodeId: String, edgeType: String?): List<GraphEdge>
  fun incoming(nodeId: String, edgeType: String?): List<GraphEdge>
  fun nodes(type: String?): List<GraphNode>
  fun edges(type: String?): List<GraphEdge>
  fun updateNode(id: String, properties: Map<String, TShellValue>)
  fun updateEdge(id: String, properties: Map<String, TShellValue>)
  fun putNode(node: GraphNode)   // upsert for merge/import
  fun putEdge(edge: GraphEdge)   // upsert for merge/import
}
```
