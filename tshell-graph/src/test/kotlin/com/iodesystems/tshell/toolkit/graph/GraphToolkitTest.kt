package com.iodesystems.tshell.toolkit.graph

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.Assert
import org.testng.annotations.Test

// Local test helpers (JUnit arg order: expected, actual)
private fun assertEquals(expected: Any?, actual: Any?) = Assert.assertEquals(actual, expected)
private fun assertEquals(expected: Int, actual: Int) = Assert.assertEquals(actual, expected)
private fun assertNotNull(obj: Any?) = Assert.assertNotNull(obj)
private fun assertTrue(condition: Boolean) = Assert.assertTrue(condition)
private inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
  try { block() } catch (e: Throwable) {
    if (e is T) return e
    throw AssertionError("Expected ${T::class.java.name} but got ${e::class.java.name}: ${e.message}", e)
  }
  throw AssertionError("Expected ${T::class.java.name} to be thrown")
}

class GraphToolkitTest {

  private fun shell(maxTraversals: Int = 10_000, schema: GraphSchema? = null): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    GraphToolkit(InMemoryGraphStore(), schema = schema, maxTraversals = maxTraversals).install(sh)
    return sh
  }

  // --- Root ---

  @Test fun `root returns root node`() {
    val sh = shell()
    val r = sh.eval("root()") as TObject
    assertEquals(TString("root"), r.fields["type"])
    assertNotNull(r.fields["id"])
  }

  // --- addNode ---

  @Test fun `addNode creates node connected to parent`() {
    val sh = shell()
    val alice = sh.eval("""addNode(root(), "person", {name: "Alice"})""") as TObject
    assertEquals(TString("person"), alice.fields["type"])
    assertEquals(TString("Alice"), alice.fields["name"])
    assertNotNull(alice.fields["id"])
  }

  @Test fun `addNode reachable via out from parent`() {
    val sh = shell()
    sh.eval("""addNode(root(), "person", {name: "Alice"})""")
    val result = sh.eval("""root() |> out("person") |> map(n => n.name)""") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("Alice"), result.elements[0])
  }

  @Test fun `addNode fails on missing parent`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("""addNode("bogus", "x", {})""") }
    assertTrue(err.message!!.contains("not found"))
  }

  // --- Multi-hop traversal ---

  @Test fun `multi-hop traversal`() {
    val sh = shell()
    sh.eval("""
      let alice = addNode(root(), "person", {name: "Alice"})
      let proj = addNode(alice, "project", {name: "TShell"})
    """)
    val result = sh.eval("""root() |> out("person") |> out("project") |> map(p => p.name)""") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("TShell"), result.elements[0])
  }

  @Test fun `out without type returns all children`() {
    val sh = shell()
    sh.eval("""
      addNode(root(), "person", {name: "Alice"})
      addNode(root(), "org", {name: "Acme"})
    """)
    val result = sh.eval("root() |> out()") as TArray
    assertEquals(2, result.elements.size)
  }

  // --- link / unlink ---

  @Test fun `link creates additional edge`() {
    val sh = shell()
    sh.eval("""
      let alice = addNode(root(), "person", {name: "Alice"})
      let bob = addNode(root(), "person", {name: "Bob"})
      link(alice, bob, "knows")
    """)
    val result = sh.eval("""nodes("person") |> filter(n => n.name == "Alice") |> out("knows") |> map(n => n.name)""") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("Bob"), result.elements[0])
  }

  @Test fun `link returns edge object`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "x", {})
      let b = addNode(root(), "x", {})
    """)
    val edge = sh.eval("""link(a, b, "rel", {weight: 0.5})""") as TObject
    assertEquals(TString("rel"), edge.fields["type"])
    assertEquals(TNumber(0.5), edge.fields["weight"])
    assertNotNull(edge.fields["id"])
  }

  @Test fun `unlink removes edge`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "x", {})
      let b = addNode(root(), "x", {})
      let e = link(a, b, "rel")
      unlink(e)
    """)
    val result = sh.eval("nodes(\"x\") |> out(\"rel\")") as TArray
    assertEquals(0, result.elements.size)
  }

  // --- removeNode ---

  @Test fun `removeNode removes node and edges`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "person", {name: "Alice"})
      removeNode(a)
    """)
    val result = sh.eval("""root() |> out("person")""") as TArray
    assertEquals(0, result.elements.size)
  }

  @Test fun `removeNode cannot remove root`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("removeNode(root())") }
    assertTrue(err.message!!.contains("root"))
  }

  // --- node / nodes ---

  @Test fun `node retrieves by id`() {
    val sh = shell()
    sh.eval("""let a = addNode(root(), "person", {name: "Alice"})""")
    val result = sh.eval("node(a.id)") as TObject
    assertEquals(TString("Alice"), result.fields["name"])
  }

  @Test fun `nodes filters by type`() {
    val sh = shell()
    sh.eval("""
      addNode(root(), "person", {name: "Alice"})
      addNode(root(), "person", {name: "Bob"})
      addNode(root(), "org", {name: "Acme"})
    """)
    val people = sh.eval("""nodes("person")""") as TArray
    assertEquals(2, people.elements.size)
    val all = sh.eval("nodes()") as TArray
    assertEquals(4, all.elements.size) // root + 3
  }

  // --- setProps ---

  @Test fun `setProps updates node`() {
    val sh = shell()
    sh.eval("""let a = addNode(root(), "person", {name: "Alice", age: 30})""")
    val updated = sh.eval("""setProps(a, {age: 31})""") as TObject
    assertEquals(TNumber(31.0), updated.fields["age"])
    assertEquals(TString("Alice"), updated.fields["name"])
  }

  @Test fun `setProps updates edge`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "x", {})
      let b = addNode(root(), "x", {})
      let e = link(a, b, "rel", {weight: 0.5})
    """)
    val updated = sh.eval("""setProps(e, {weight: 0.9})""") as TObject
    assertEquals(TNumber(0.9), updated.fields["weight"])
  }

  // --- inbound ---

  @Test fun `inbound follows incoming edges`() {
    val sh = shell()
    sh.eval("""
      let alice = addNode(root(), "person", {name: "Alice"})
      let bob = addNode(root(), "person", {name: "Bob"})
      link(alice, bob, "knows")
    """)
    val result = sh.eval("""
      nodes("person") |> filter(n => n.name == "Bob") |> inbound("knows") |> map(n => n.name)
    """) as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("Alice"), result.elements[0])
  }

  // --- both ---

  @Test fun `both follows edges in both directions`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "person", {name: "A"})
      let b = addNode(root(), "person", {name: "B"})
      let c = addNode(root(), "person", {name: "C"})
      link(a, b, "friend")
      link(c, a, "friend")
    """)
    val result = sh.eval("""
      nodes("person") |> filter(n => n.name == "A") |> both("friend") |> map(n => n.name) |> sort()
    """) as TArray
    assertEquals(2, result.elements.size)
    assertEquals(TString("B"), result.elements[0])
    assertEquals(TString("C"), result.elements[1])
  }

  // --- outE / inE ---

  @Test fun `outE returns edge objects`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "person", {name: "Alice"})
      let b = addNode(root(), "person", {name: "Bob"})
      link(a, b, "knows", {since: 2020})
    """)
    val result = sh.eval("""
      nodes("person") |> filter(n => n.name == "Alice") |> outE("knows")
    """) as TArray
    assertEquals(1, result.elements.size)
    val edge = result.elements[0] as TObject
    assertEquals(TString("knows"), edge.fields["type"])
    assertEquals(TNumber(2020.0), edge.fields["since"])
  }

  @Test fun `inE returns incoming edge objects`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "person", {name: "Alice"})
      let b = addNode(root(), "person", {name: "Bob"})
      link(a, b, "knows")
    """)
    val result = sh.eval("""
      nodes("person") |> filter(n => n.name == "Bob") |> inE("knows")
    """) as TArray
    assertEquals(1, result.elements.size)
  }

  // --- Integration with tshell pipes ---

  @Test fun `filter and map compose with traversal`() {
    val sh = shell()
    sh.eval("""
      addNode(root(), "person", {name: "Alice", age: 30})
      addNode(root(), "person", {name: "Bob", age: 25})
      addNode(root(), "person", {name: "Carol", age: 35})
    """)
    val result = sh.eval("""
      root() |> out("person") |> filter(n => n.age > 28) |> map(n => n.name) |> sort()
    """) as TArray
    assertEquals(2, result.elements.size)
    assertEquals(TString("Alice"), result.elements[0])
    assertEquals(TString("Carol"), result.elements[1])
  }

  @Test fun `reduce on traversal results`() {
    val sh = shell()
    sh.eval("""
      addNode(root(), "item", {value: 10})
      addNode(root(), "item", {value: 20})
      addNode(root(), "item", {value: 30})
    """)
    val result = sh.eval("""
      root() |> out("item") |> map(n => n.value) |> reduce((sum, v) => sum + v, 0)
    """)
    assertEquals(TNumber(60.0), result)
  }

  // --- Traversal limit ---

  @Test fun `traversal limit exceeded`() {
    val sh = shell(maxTraversals = 50)
    sh.eval("""
      for (let i of range(0, 100)) {
        addNode(root(), "item", {n: i})
      }
    """)
    val err = assertThrows<TShellError> { sh.eval("""root() |> out("item")""") }
    assertTrue(err.message!!.contains("traversal limit"))
  }

  @Test fun `traversal counter resets on new query`() {
    val sh = shell(maxTraversals = 50)
    sh.eval("""
      for (let i of range(0, 30)) {
        addNode(root(), "item", {n: i})
      }
    """)
    // First query uses 30 traversals
    sh.eval("""root() |> out("item")""")
    // Second query should also work (counter reset by root())
    val result = sh.eval("""root() |> out("item")""") as TArray
    assertEquals(30, result.elements.size)
  }

  // --- Complex graph patterns ---

  @Test fun `diamond traversal deduplication`() {
    val sh = shell()
    sh.eval("""
      let a = addNode(root(), "node", {name: "A"})
      let b = addNode(a, "node", {name: "B"})
      let c = addNode(a, "node", {name: "C"})
      let d = addNode(b, "node", {name: "D"})
      link(c, d)
    """)
    // A -> B -> D and A -> C -> D
    // Going out from A's children, D should appear twice (from B and C)
    val result = sh.eval("""
      node(a.id) |> out() |> out() |> map(n => n.name)
    """) as TArray
    assertEquals(2, result.elements.size) // D from B, D from C
  }

  @Test fun `graph with typed edges for relationships`() {
    val sh = shell()
    sh.eval("""
      let alice = addNode(root(), "person", {name: "Alice"})
      let acme = addNode(root(), "company", {name: "Acme"})
      link(alice, acme, "worksAt")
      let proj = addNode(acme, "project", {name: "TShell"})
      link(alice, proj, "leads")
    """)
    // Alice works at Acme
    val company = sh.eval("""
      nodes("person") |> filter(n => n.name == "Alice") |> out("worksAt") |> map(n => n.name)
    """) as TArray
    assertEquals(TString("Acme"), company.elements[0])
    // Alice leads TShell
    val project = sh.eval("""
      nodes("person") |> filter(n => n.name == "Alice") |> out("leads") |> map(n => n.name)
    """) as TArray
    assertEquals(TString("TShell"), project.elements[0])
  }

  // --- Schema enforcement ---

  @Test fun `schema rejects node missing required property`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name")))
    ))
    val err = assertThrows<TShellError> { sh.eval("""addNode(root(), "person", {age: 30})""") }
    assertTrue(err.message!!.contains("missing required"))
    assertTrue(err.message!!.contains("name"))
  }

  @Test fun `schema allows node with required properties`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name")))
    ))
    val result = sh.eval("""addNode(root(), "person", {name: "Alice"})""") as TObject
    assertEquals(TString("Alice"), result.fields["name"])
  }

  @Test fun `schema rejects unknown properties when optional is set`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name"), optional = setOf("age")))
    ))
    val err = assertThrows<TShellError> { sh.eval("""addNode(root(), "person", {name: "Alice", foo: 1})""") }
    assertTrue(err.message!!.contains("unknown properties"))
    assertTrue(err.message!!.contains("foo"))
  }

  @Test fun `schema allows extra properties when optional is null`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name")))
    ))
    val result = sh.eval("""addNode(root(), "person", {name: "Alice", anything: "ok"})""") as TObject
    assertEquals(TString("ok"), result.fields["anything"])
  }

  @Test fun `schema rejects wrong edge source type`() {
    val sh = shell(schema = GraphSchema(
      edges = mapOf("worksAt" to EdgeSchema(from = "person", to = "company"))
    ))
    sh.eval("""
      let acme = addNode(root(), "company", {name: "Acme"})
      let other = addNode(root(), "company", {name: "Other"})
    """)
    val err = assertThrows<TShellError> { sh.eval("""link(acme, other, "worksAt")""") }
    assertTrue(err.message!!.contains("must originate from 'person'"))
  }

  @Test fun `schema rejects wrong edge target type`() {
    val sh = shell(schema = GraphSchema(
      edges = mapOf("worksAt" to EdgeSchema(from = "person", to = "company"))
    ))
    sh.eval("""
      let alice = addNode(root(), "person", {name: "Alice"})
      let bob = addNode(root(), "person", {name: "Bob"})
    """)
    val err = assertThrows<TShellError> { sh.eval("""link(alice, bob, "worksAt")""") }
    assertTrue(err.message!!.contains("must target 'company'"))
  }

  @Test fun `schema allows valid edge`() {
    val sh = shell(schema = GraphSchema(
      edges = mapOf("worksAt" to EdgeSchema(from = "person", to = "company"))
    ))
    sh.eval("""
      let alice = addNode(root(), "person", {name: "Alice"})
      let acme = addNode(root(), "company", {name: "Acme"})
      link(alice, acme, "worksAt")
    """)
    val result = sh.eval("""nodes("person") |> out("worksAt") |> map(n => n.name)""") as TArray
    assertEquals(TString("Acme"), result.elements[0])
  }

  @Test fun `strict schema rejects unknown node type`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name"))),
      strict = true
    ))
    val err = assertThrows<TShellError> { sh.eval("""addNode(root(), "alien", {name: "X"})""") }
    assertTrue(err.message!!.contains("unknown node type"))
  }

  @Test fun `strict schema rejects unknown edge type`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name"))),
      edges = mapOf("knows" to EdgeSchema(from = "person", to = "person")),
      strict = true
    ))
    sh.eval("""
      let a = addNode(root(), "person", {name: "A"})
      let b = addNode(root(), "person", {name: "B"})
    """)
    val err = assertThrows<TShellError> { sh.eval("""link(a, b, "invented")""") }
    assertTrue(err.message!!.contains("unknown edge type"))
  }

  @Test fun `schema validates setProps does not add unknown properties`() {
    val sh = shell(schema = GraphSchema(
      nodes = mapOf("person" to NodeSchema(required = setOf("name"), optional = setOf("age")))
    ))
    sh.eval("""let a = addNode(root(), "person", {name: "Alice"})""")
    val err = assertThrows<TShellError> { sh.eval("""setProps(a, {badProp: 1})""") }
    assertTrue(err.message!!.contains("unknown properties"))
  }

  @Test fun `no schema means no enforcement`() {
    val sh = shell()
    // Should all work without any schema
    sh.eval("""
      let a = addNode(root(), "anything", {whatever: 1})
      let b = addNode(root(), "other", {})
      link(a, b, "randomEdge")
    """)
    val result = sh.eval("""nodes("anything") |> out("randomEdge") |> map(n => n.type)""") as TArray
    assertEquals(TString("other"), result.elements[0])
  }
}
