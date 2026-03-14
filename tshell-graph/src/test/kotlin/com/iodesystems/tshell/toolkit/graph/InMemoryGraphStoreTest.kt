package com.iodesystems.tshell.toolkit.graph

import com.iodesystems.tshell.runtime.TShellValue.*
import org.testng.Assert
import org.testng.annotations.Test

// Local test helpers (JUnit arg order: expected, actual)
private fun assertEquals(expected: Any?, actual: Any?) = Assert.assertEquals(actual, expected)
private fun assertEquals(expected: Int, actual: Int) = Assert.assertEquals(actual, expected)
private fun assertNotNull(obj: Any?) = Assert.assertNotNull(obj)
private fun assertNull(obj: Any?) = Assert.assertNull(obj)
private fun assertNotEquals(a: Any?, b: Any?) = Assert.assertNotEquals(a, b)

class InMemoryGraphStoreTest {

  // --- Fork isolation ---

  @Test fun `fork sees delegate nodes`() {
    val main = InMemoryGraphStore()
    val node = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, node.id, "person")

    val fork = main.fork()
    val found = fork.getNode(node.id)
    assertNotNull(found)
    assertEquals("Alice", (found!!.properties["name"] as TString).value)
  }

  @Test fun `fork writes are invisible to delegate`() {
    val main = InMemoryGraphStore()
    val fork = main.fork()

    val node = fork.addNode("person", mapOf("name" to TString("Bob")))
    fork.addEdge(fork.rootId, node.id, "person")

    // Fork sees it
    assertNotNull(fork.getNode(node.id))
    assertEquals(2, fork.nodes().size) // root + bob

    // Main doesn't
    assertNull(main.getNode(node.id))
    assertEquals(1, main.nodes().size) // root only
  }

  @Test fun `fork deletes are invisible to delegate`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    fork.removeNode(alice.id)

    // Fork doesn't see Alice
    assertNull(fork.getNode(alice.id))
    assertEquals(0, fork.outgoing(fork.rootId, "person").size)

    // Main still has Alice
    assertNotNull(main.getNode(alice.id))
    assertEquals(1, main.outgoing(main.rootId, "person").size)
  }

  @Test fun `fork updates are isolated via copy-on-write`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice"), "age" to TNumber(30.0)))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    fork.updateNode(alice.id, mapOf("age" to TNumber(31.0)))

    // Fork sees updated age
    assertEquals(TNumber(31.0), fork.getNode(alice.id)!!.properties["age"])

    // Main still has original age
    assertEquals(TNumber(30.0), main.getNode(alice.id)!!.properties["age"])
  }

  // --- Merge ---

  @Test fun `merge applies new nodes to delegate`() {
    val main = InMemoryGraphStore()
    val fork = main.fork()

    val bob = fork.addNode("person", mapOf("name" to TString("Bob")))
    fork.addEdge(fork.rootId, bob.id, "person")
    fork.merge()

    // Main now has Bob
    assertNotNull(main.getNode(bob.id))
    assertEquals(1, main.outgoing(main.rootId, "person").size)
  }

  @Test fun `merge applies deletions to delegate`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    fork.removeNode(alice.id)
    fork.merge()

    // Main no longer has Alice
    assertNull(main.getNode(alice.id))
    assertEquals(0, main.outgoing(main.rootId, "person").size)
  }

  @Test fun `merge applies updates to delegate`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice"), "age" to TNumber(30.0)))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    fork.updateNode(alice.id, mapOf("age" to TNumber(31.0)))
    fork.merge()

    assertEquals(TNumber(31.0), main.getNode(alice.id)!!.properties["age"])
  }

  @Test fun `merge clears overlay state`() {
    val main = InMemoryGraphStore()
    val fork = main.fork()

    val bob = fork.addNode("person", mapOf("name" to TString("Bob")))
    fork.addEdge(fork.rootId, bob.id, "person")
    fork.merge()

    // Fork is now empty overlay — should only see delegate state
    val allNodes = fork.nodes()
    assertEquals(main.nodes().size, allNodes.size)
  }

  // --- Discard ---

  @Test fun `discard throws away overlay changes`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    fork.addNode("person", mapOf("name" to TString("Bob")))
    fork.removeNode(alice.id)
    fork.discard()

    // Fork is back to delegate state
    assertNotNull(fork.getNode(alice.id))
    assertEquals(main.nodes().size, fork.nodes().size)
  }

  // --- Traversal through overlay ---

  @Test fun `outgoing merges local and delegate edges`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    val bob = fork.addNode("person", mapOf("name" to TString("Bob")))
    fork.addEdge(fork.rootId, bob.id, "person")

    // Root has edges to both Alice (delegate) and Bob (overlay)
    val edges = fork.outgoing(fork.rootId, "person")
    assertEquals(2, edges.size)
  }

  @Test fun `nodes merges local and delegate without duplicates`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, alice.id, "person")

    val fork = main.fork()
    // Update Alice (copy-on-write brings her into local map)
    fork.updateNode(alice.id, mapOf("age" to TNumber(30.0)))
    // Add Bob
    fork.addNode("person", mapOf("name" to TString("Bob")))

    // Should have root + Alice + Bob = 3, no duplicate Alice
    val all = fork.nodes()
    assertEquals(3, all.size)
    val people = fork.nodes("person")
    assertEquals(2, people.size)
  }

  // --- Edge cases ---

  @Test fun `fork of fork works`() {
    val main = InMemoryGraphStore()
    val alice = main.addNode("person", mapOf("name" to TString("Alice")))
    main.addEdge(main.rootId, alice.id, "person")

    val fork1 = main.fork()
    val bob = fork1.addNode("person", mapOf("name" to TString("Bob")))
    fork1.addEdge(fork1.rootId, bob.id, "person")

    val fork2 = fork1.fork()
    val carol = fork2.addNode("person", mapOf("name" to TString("Carol")))
    fork2.addEdge(fork2.rootId, carol.id, "person")

    // fork2 sees all three
    assertEquals(3, fork2.nodes("person").size)

    // fork1 sees two
    assertEquals(2, fork1.nodes("person").size)

    // main sees one
    assertEquals(1, main.nodes("person").size)

    // Merge fork2 into fork1
    fork2.merge()
    assertEquals(3, fork1.nodes("person").size)

    // Merge fork1 into main
    fork1.merge()
    assertEquals(3, main.nodes("person").size)
  }

  @Test fun `IDs are unique across forks`() {
    val main = InMemoryGraphStore()
    val fork1 = main.fork()
    val fork2 = main.fork()

    val a = fork1.addNode("x")
    val b = fork2.addNode("x")

    assertNotEquals(a.id, b.id)
  }
}
