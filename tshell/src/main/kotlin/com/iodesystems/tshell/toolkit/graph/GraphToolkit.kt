package com.iodesystems.tshell.toolkit.graph

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*

class GraphToolkit(
  private val store: GraphStore,
  private val schema: GraphSchema? = null,
  private val maxTraversals: Int = 10_000
) {
  private var traversalCount = 0

  private fun resetTraversals() {
    traversalCount = 0
  }

  private fun countTraversal(n: Int) {
    traversalCount += n
    if (traversalCount > maxTraversals) {
      throw TShellError(
        "Graph traversal limit exceeded ($maxTraversals steps)\n\n" +
          "  Use a more specific edge type filter, or increase the limit."
      )
    }
  }

  fun install(shell: TShell): TShell {

    shell.registerGuide("graph", """
Graph Toolkit — nodes, edges, and traversal

All nodes connect to root. Traversal steps (out, inbound, both) work as
pipes: they accept a single node or an array and return an array. This
means filter(), map(), reduce(), sort() all compose naturally.

TYPICAL: Build a graph
  val alice = addNode(root(), "person", {name: "Alice", age: 30})
  val bob = addNode(root(), "person", {name: "Bob", age: 25})
  val acme = addNode(root(), "company", {name: "Acme"})
  link(alice, acme, "worksAt")
  link(bob, acme, "worksAt")
  link(alice, bob, "knows", {since: 2020})

TYPICAL: Traverse and query
  root() |> out("person")                              // all people
  root() |> out("person") |> filter(n => n.age > 28)    // filtered
  root() |> out("person") |> map(n => n.name) |> sort()  // sorted names

  // Multi-hop: root → person → company
  root() |> out("person") |> out("worksAt") |> map(c => c.name)

  // Reverse: who works at Acme?
  nodes("company") |> filter(c => c.name == "Acme") |> inbound("worksAt") |> map(n => n.name)

TYPICAL: Edge inspection
  node(alice.id) |> outE("knows")
  // → [{id: "e3", from: "n2", to: "n3", type: "knows", since: 2020}]

  node(alice.id) |> outE() |> filter(e => e.type == "worksAt")

TYPICAL: CRUD operations
  setProps(alice, {age: 31})           // update node properties
  setProps("e3", {since: 2019})        // update edge properties
  removeNode(bob)                      // removes node + all edges
  unlink("e3")                         // remove single edge
  node(alice.id)                       // get node by id
  nodes("person")                      // all nodes of type

ADVANCED: Bidirectional traversal
  // All nodes connected to alice in either direction
  node(alice.id) |> both("knows") |> map(n => n.name)

ADVANCED: Graph analytics
  // Degree count: how many outgoing edges per person
  nodes("person") |> map(p => {
    name: p.name,
    connections: (node(p.id) |> outE() |> len())
  }) |> sort("connections") |> reverse()

ADVANCED: Path queries
  // Find all projects led by people who work at Acme
  nodes("company")
    |> filter(c => c.name == "Acme")
    |> inbound("worksAt")
    |> out("leads")
    |> map(p => p.name)

ADVANCED: Aggregation across graph
  // Total age of all people in the graph
  nodes("person") |> map(p => p.age) |> reduce((sum, a) => sum + a, 0)

  // Group by company
  nodes("company") |> map(c => {
    company: c.name,
    employees: (node(c.id) |> inbound("worksAt") |> map(e => e.name))
  })
""".trimIndent() + if (schema != null) "\n\n${schema.toGuide()}" else "")

    // --- Query start points ---

    shell.register("root", "", "returns the graph root node",
      listOf("root()", """root() |> out("person")""")
    ) { _ ->
      resetTraversals()
      nodeToObj(store.getNode(store.rootId)!!)
    }

    shell.register("node", "id: string", "gets a node by id",
      listOf("""node("n1")""", """val n = node(id); n.name""")
    ) { args ->
      resetTraversals()
      val id = extractId(args.getOrElse(0) { TNull }, "node")
      val n = store.getNode(id) ?: throw TShellError("node: not found '$id'")
      nodeToObj(n)
    }

    shell.register("nodes", "type?: string", "gets all nodes, optionally filtered by type",
      listOf("""nodes("person")""", """nodes() |> filter(n => n.age > 25)""")
    ) { args ->
      resetTraversals()
      val type = (args.firstOrNull() as? TString)?.value
      TArray(store.nodes(type).map { nodeToObj(it) })
    }

    // --- CRUD ---

    shell.register(
      "addNode", "parent: node|string, type: string, props?: object",
      "adds a node connected from parent. Edge type defaults to node type",
      listOf(
        """addNode(root(), "person", {name: "Alice"})""",
        """val org = addNode(root(), "org", {name: "Acme"})""",
        """addNode(org, "member", {name: "Bob"})""",
      )
    ) { args ->
      val parentId = extractId(args.getOrElse(0) { TNull }, "addNode parent")
      val type = (args.getOrElse(1) { TNull } as? TString)?.value
        ?: throw TShellError.wrongArguments("addNode", "parent, type: string, props?: object", args,
          """addNode(root(), "person", {name: "Alice"})""")
      val props = extractProps(args.getOrElse(2) { TNull })
      if (store.getNode(parentId) == null) {
        throw TShellError("addNode: parent node '$parentId' not found")
      }
      schema?.validateNode(type, props)
      val node = store.addNode(type, props)
      store.addEdge(parentId, node.id, type)
      nodeToObj(node)
    }

    shell.register(
      "link", "from: node|string, to: node|string, type?: string, props?: object",
      "creates an edge between two nodes",
      listOf(
        """link(alice, bob, "knows")""",
        """link(alice, project, "owns", {since: 2024})""",
      )
    ) { args ->
      val fromId = extractId(args.getOrElse(0) { TNull }, "link from")
      val toId = extractId(args.getOrElse(1) { TNull }, "link to")
      val type = (args.getOrElse(2) { TNull } as? TString)?.value
      val props = extractProps(args.getOrElse(3) { TNull })
      val fromNode = store.getNode(fromId) ?: throw TShellError("link: source node '$fromId' not found")
      val toNode = store.getNode(toId) ?: throw TShellError("link: target node '$toId' not found")
      schema?.validateEdge(type, fromNode.type, toNode.type)
      edgeToObj(store.addEdge(fromId, toId, type, props))
    }

    shell.register(
      "unlink", "edge: edge|string",
      "removes an edge by id",
      listOf("""unlink(edgeId)""", """unlink("e1")""")
    ) { args ->
      val id = extractId(args.getOrElse(0) { TNull }, "unlink")
      if (store.getEdge(id) == null) throw TShellError("unlink: edge '$id' not found")
      store.removeEdge(id)
      TNull
    }

    shell.register(
      "removeNode", "node: node|string",
      "removes a node and all its edges",
      listOf("""removeNode(nodeId)""", """removeNode("n3")""")
    ) { args ->
      val id = extractId(args.getOrElse(0) { TNull }, "removeNode")
      if (id == store.rootId) throw TShellError("removeNode: cannot remove root node")
      if (store.getNode(id) == null) throw TShellError("removeNode: node '$id' not found")
      store.removeNode(id)
      TNull
    }

    shell.register(
      "setProps", "target: node|edge|string, props: object",
      "merges properties into a node or edge",
      listOf("""setProps(alice, {age: 31})""", """setProps("e1", {weight: 0.5})""")
    ) { args ->
      val id = extractId(args.getOrElse(0) { TNull }, "setProps target")
      val props = (args.getOrElse(1) { TNull } as? TObject)?.fields
        ?: throw TShellError.wrongArguments("setProps", "target, props: object", args,
          """setProps(node, {key: value})""")
      val node = store.getNode(id)
      if (node != null) {
        if (schema != null) {
          val merged = node.properties.keys + props.keys
          schema.validateNode(node.type, merged.associateWith { })
        }
        store.updateNode(id, props)
        return@register nodeToObj(store.getNode(id)!!)
      }
      val edge = store.getEdge(id)
      if (edge != null) {
        store.updateEdge(id, props)
        return@register edgeToObj(store.getEdge(id)!!)
      }
      throw TShellError("setProps: no node or edge with id '$id'")
    }

    // --- Traversal steps (pipe-compatible) ---

    shell.register("out", "input: node|array, type?: string", "follows outgoing edges, returns target nodes",
      listOf("""root() |> out("person")""", """node |> out()""")
    ) { args ->
      val input = args[0]
      val edgeType = (args.getOrNull(1) as? TString)?.value
      val sources = toNodeList(input, "out")
      val results = mutableListOf<TShellValue>()
      for (obj in sources) {
        val nodeId = objId(obj)
        val edges = store.outgoing(nodeId, edgeType)
        countTraversal(edges.size)
        for (edge in edges) {
          val target = store.getNode(edge.to) ?: continue
          results.add(nodeToObj(target))
        }
      }
      TArray(results)
    }

    shell.register("inbound", "input: node|array, type?: string", "follows incoming edges, returns source nodes",
      listOf("""node |> inbound("knows")""", """nodes("project") |> inbound()""")
    ) { args ->
      val input = args[0]
      val edgeType = (args.getOrNull(1) as? TString)?.value
      val sources = toNodeList(input, "inbound")
      val results = mutableListOf<TShellValue>()
      for (obj in sources) {
        val nodeId = objId(obj)
        val edges = store.incoming(nodeId, edgeType)
        countTraversal(edges.size)
        for (edge in edges) {
          val source = store.getNode(edge.from) ?: continue
          results.add(nodeToObj(source))
        }
      }
      TArray(results)
    }

    shell.register("both", "input: node|array, type?: string", "follows edges in both directions",
      listOf("""node |> both("knows")""")
    ) { args ->
      val input = args[0]
      val edgeType = (args.getOrNull(1) as? TString)?.value
      val sources = toNodeList(input, "both")
      val seen = mutableSetOf<String>()
      val results = mutableListOf<TShellValue>()
      for (obj in sources) {
        val nodeId = objId(obj)
        val outEdges = store.outgoing(nodeId, edgeType)
        val inEdges = store.incoming(nodeId, edgeType)
        countTraversal(outEdges.size + inEdges.size)
        for (edge in outEdges) {
          if (seen.add(edge.to)) {
            val target = store.getNode(edge.to) ?: continue
            results.add(nodeToObj(target))
          }
        }
        for (edge in inEdges) {
          if (seen.add(edge.from)) {
            val source = store.getNode(edge.from) ?: continue
            results.add(nodeToObj(source))
          }
        }
      }
      TArray(results)
    }

    shell.register("outE", "input: node|array, type?: string", "gets outgoing edges as objects",
      listOf("""node |> outE("knows")""", """root() |> outE()""")
    ) { args ->
      val input = args[0]
      val edgeType = (args.getOrNull(1) as? TString)?.value
      val sources = toNodeList(input, "outE")
      val results = mutableListOf<TShellValue>()
      for (obj in sources) {
        val nodeId = objId(obj)
        val edges = store.outgoing(nodeId, edgeType)
        countTraversal(edges.size)
        for (edge in edges) {
          results.add(edgeToObj(edge))
        }
      }
      TArray(results)
    }

    shell.register("inE", "input: node|array, type?: string", "gets incoming edges as objects",
      listOf("""node |> inE("knows")""")
    ) { args ->
      val input = args[0]
      val edgeType = (args.getOrNull(1) as? TString)?.value
      val sources = toNodeList(input, "inE")
      val results = mutableListOf<TShellValue>()
      for (obj in sources) {
        val nodeId = objId(obj)
        val edges = store.incoming(nodeId, edgeType)
        countTraversal(edges.size)
        for (edge in edges) {
          results.add(edgeToObj(edge))
        }
      }
      TArray(results)
    }

    return shell
  }

  // --- Conversion helpers ---

  private fun nodeToObj(node: GraphNode): TObject {
    val fields = mutableMapOf<String, TShellValue>(
      "id" to TString(node.id),
      "type" to TString(node.type)
    )
    fields.putAll(node.properties)
    return TObject(fields)
  }

  private fun edgeToObj(edge: GraphEdge): TObject {
    val fields = mutableMapOf<String, TShellValue>(
      "id" to TString(edge.id),
      "from" to TString(edge.from),
      "to" to TString(edge.to),
      "type" to (if (edge.type != null) TString(edge.type) else TNull)
    )
    fields.putAll(edge.properties)
    return TObject(fields)
  }

  private fun extractId(value: TShellValue, context: String): String {
    return when (value) {
      is TString -> value.value
      is TObject -> (value.fields["id"] as? TString)?.value
        ?: throw TShellError("$context: object has no 'id' field")
      else -> throw TShellError("$context: expected node/edge object or id string, got ${value.typeName()}")
    }
  }

  private fun extractProps(value: TShellValue): Map<String, TShellValue> {
    return when (value) {
      is TObject -> value.fields
      is TNull -> emptyMap()
      else -> throw TShellError("Expected object for properties, got ${value.typeName()}")
    }
  }

  private fun objId(obj: TObject): String {
    return (obj.fields["id"] as? TString)?.value
      ?: throw TShellError("Graph traversal: object missing 'id' field")
  }

  private fun toNodeList(input: TShellValue, step: String): List<TObject> {
    return when (input) {
      is TObject -> listOf(input)
      is TArray -> input.elements.map { el ->
        el as? TObject ?: throw TShellError.typeMismatch(step, "node objects", el)
      }
      else -> throw TShellError.typeMismatch(step, "node or array of nodes", input)
    }
  }

}
