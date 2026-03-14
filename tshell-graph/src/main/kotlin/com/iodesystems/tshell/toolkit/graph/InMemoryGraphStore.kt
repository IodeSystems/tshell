package com.iodesystems.tshell.toolkit.graph

import com.iodesystems.tshell.runtime.TShellValue
import java.util.UUID

class InMemoryGraphStore(
  private val delegate: GraphStore? = null
) : GraphStore {
  private val nodeMap = mutableMapOf<String, GraphNode>()
  private val edgeMap = mutableMapOf<String, GraphEdge>()
  private val outEdges = mutableMapOf<String, MutableList<String>>()
  private val inEdges = mutableMapOf<String, MutableList<String>>()

  // Tombstones track deletions in the overlay so delegate reads are filtered
  private val tombstonedNodes = mutableSetOf<String>()
  private val tombstonedEdges = mutableSetOf<String>()

  override val rootId: String

  init {
    if (delegate != null) {
      rootId = delegate.rootId
    } else {
      val id = nextId()
      nodeMap[id] = GraphNode(id, "root")
      outEdges[id] = mutableListOf()
      inEdges[id] = mutableListOf()
      rootId = id
    }
  }

  // --- Fork / Merge / Discard ---

  private fun nextId(): String = UUID.randomUUID().toString()

  /**
   * Creates an overlay that captures all changes without modifying this store.
   * Reads fall through to this store. Writes and deletes stay in the overlay.
   */
  fun fork(): InMemoryGraphStore {
    return InMemoryGraphStore(delegate = this)
  }

  /**
   * Applies this overlay's changes (adds, updates, tombstones) to the delegate.
   * After merge, this overlay is cleared.
   */
  fun merge() {
    val d = delegate ?: throw IllegalStateException("Cannot merge: no delegate store")

    // Apply node adds/updates — order matters: nodes before edges
    for (node in nodeMap.values) {
      d.putNode(GraphNode(node.id, node.type, node.properties.toMutableMap()))
    }

    // Apply edge adds/updates
    for (edge in edgeMap.values) {
      d.putEdge(GraphEdge(edge.id, edge.from, edge.to, edge.type, edge.properties.toMutableMap()))
    }

    // Apply tombstones — edges before nodes (edges reference nodes)
    for (edgeId in tombstonedEdges) {
      d.removeEdge(edgeId)
    }
    for (nodeId in tombstonedNodes) {
      d.removeNode(nodeId)
    }

    discard()
  }

  /**
   * Throws away all overlay changes without affecting the delegate.
   */
  fun discard() {
    nodeMap.clear()
    edgeMap.clear()
    outEdges.clear()
    inEdges.clear()
    tombstonedNodes.clear()
    tombstonedEdges.clear()
  }

  // --- Reads: local first, then delegate (minus tombstones) ---

  override fun getNode(id: String): GraphNode? {
    if (id in tombstonedNodes) return null
    return nodeMap[id] ?: delegate?.getNode(id)
  }

  override fun getEdge(id: String): GraphEdge? {
    if (id in tombstonedEdges) return null
    return edgeMap[id] ?: delegate?.getEdge(id)
  }

  override fun outgoing(nodeId: String, edgeType: String?): List<GraphEdge> {
    if (nodeId in tombstonedNodes) return emptyList()
    val local = outEdges[nodeId].orEmpty().mapNotNull { edgeMap[it] }
    val delegateEdges = delegate?.outgoing(nodeId, edgeType)
      ?.filter { it.id !in tombstonedEdges && it.id !in edgeMap }
      ?: emptyList()
    val all = local + delegateEdges
    return if (edgeType != null) all.filter { it.type == edgeType } else all
  }

  override fun incoming(nodeId: String, edgeType: String?): List<GraphEdge> {
    if (nodeId in tombstonedNodes) return emptyList()
    val local = inEdges[nodeId].orEmpty().mapNotNull { edgeMap[it] }
    val delegateEdges = delegate?.incoming(nodeId, edgeType)
      ?.filter { it.id !in tombstonedEdges && it.id !in edgeMap }
      ?: emptyList()
    val all = local + delegateEdges
    return if (edgeType != null) all.filter { it.type == edgeType } else all
  }

  override fun nodes(type: String?): List<GraphNode> {
    val localIds = nodeMap.keys
    val local = if (type != null) nodeMap.values.filter { it.type == type } else nodeMap.values.toList()
    val delegateNodes = delegate?.nodes(type)
      ?.filter { it.id !in tombstonedNodes && it.id !in localIds }
      ?: emptyList()
    return local + delegateNodes
  }

  override fun edges(type: String?): List<GraphEdge> {
    val localIds = edgeMap.keys
    val local = if (type != null) edgeMap.values.filter { it.type == type } else edgeMap.values.toList()
    val delegateEdges = delegate?.edges(type)
      ?.filter { it.id !in tombstonedEdges && it.id !in localIds }
      ?: emptyList()
    return local + delegateEdges
  }

  // --- Writes: always local ---

  override fun addNode(type: String, properties: Map<String, TShellValue>): GraphNode {
    val id = nextId()
    val node = GraphNode(id, type, properties.toMutableMap())
    nodeMap[id] = node
    outEdges[id] = mutableListOf()
    inEdges[id] = mutableListOf()
    return node
  }

  override fun addEdge(fromId: String, toId: String, type: String?, properties: Map<String, TShellValue>): GraphEdge {
    require(getNode(fromId) != null) { "Source node '$fromId' not found" }
    require(getNode(toId) != null) { "Target node '$toId' not found" }
    val id = nextId()
    val edge = GraphEdge(id, fromId, toId, type, properties.toMutableMap())
    edgeMap[id] = edge
    outEdges.getOrPut(fromId) { mutableListOf() }.add(id)
    inEdges.getOrPut(toId) { mutableListOf() }.add(id)
    return edge
  }

  override fun updateNode(id: String, properties: Map<String, TShellValue>) {
    if (id in tombstonedNodes) throw IllegalArgumentException("Node '$id' has been deleted")
    val local = nodeMap[id]
    if (local != null) {
      local.properties.putAll(properties)
    } else {
      // Copy-on-write from delegate
      val delegateNode = delegate?.getNode(id)
        ?: throw IllegalArgumentException("Node '$id' not found")
      val copy = GraphNode(delegateNode.id, delegateNode.type, delegateNode.properties.toMutableMap())
      copy.properties.putAll(properties)
      nodeMap[id] = copy
    }
  }

  override fun updateEdge(id: String, properties: Map<String, TShellValue>) {
    if (id in tombstonedEdges) throw IllegalArgumentException("Edge '$id' has been deleted")
    val local = edgeMap[id]
    if (local != null) {
      local.properties.putAll(properties)
    } else {
      val delegateEdge = delegate?.getEdge(id)
        ?: throw IllegalArgumentException("Edge '$id' not found")
      val copy = GraphEdge(delegateEdge.id, delegateEdge.from, delegateEdge.to, delegateEdge.type, delegateEdge.properties.toMutableMap())
      copy.properties.putAll(properties)
      edgeMap[id] = copy
      outEdges.getOrPut(copy.from) { mutableListOf() }.add(id)
      inEdges.getOrPut(copy.to) { mutableListOf() }.add(id)
    }
  }

  // --- Deletes: tombstone in overlay ---

  override fun removeNode(id: String) {
    // Remove local state if present
    nodeMap.remove(id)
    val localOutEdges = outEdges.remove(id).orEmpty()
    val localInEdges = inEdges.remove(id).orEmpty()
    for (eid in (localOutEdges + localInEdges).toSet()) {
      removeEdge(eid)
    }

    // Tombstone delegate edges to/from this node
    if (delegate != null) {
      for (edge in delegate.outgoing(id)) {
        if (edge.id !in tombstonedEdges) tombstonedEdges.add(edge.id)
      }
      for (edge in delegate.incoming(id)) {
        if (edge.id !in tombstonedEdges) tombstonedEdges.add(edge.id)
      }
    }

    // Tombstone the node itself (marks delegate's copy as deleted)
    tombstonedNodes.add(id)
  }

  override fun removeEdge(id: String) {
    val local = edgeMap.remove(id)
    if (local != null) {
      outEdges[local.from]?.remove(id)
      inEdges[local.to]?.remove(id)
    }
    // Tombstone whether local or delegate
    tombstonedEdges.add(id)
  }

  // --- Put: ID-preserving upsert for merge/import ---

  override fun putNode(node: GraphNode) {
    val copy = GraphNode(node.id, node.type, node.properties.toMutableMap())
    nodeMap[node.id] = copy
    outEdges.putIfAbsent(node.id, mutableListOf())
    inEdges.putIfAbsent(node.id, mutableListOf())
    tombstonedNodes.remove(node.id)
  }

  override fun putEdge(edge: GraphEdge) {
    val existing = edgeMap[edge.id]
    if (existing != null) {
      outEdges[existing.from]?.remove(edge.id)
      inEdges[existing.to]?.remove(edge.id)
    }
    val copy = GraphEdge(edge.id, edge.from, edge.to, edge.type, edge.properties.toMutableMap())
    edgeMap[edge.id] = copy
    outEdges.getOrPut(edge.from) { mutableListOf() }.add(edge.id)
    inEdges.getOrPut(edge.to) { mutableListOf() }.add(edge.id)
    tombstonedEdges.remove(edge.id)
  }
}
