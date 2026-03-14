package com.iodesystems.tshell.toolkit.graph

import com.iodesystems.tshell.runtime.TShellValue

data class GraphNode(
  val id: String,
  val type: String,
  val properties: MutableMap<String, TShellValue> = mutableMapOf()
)

data class GraphEdge(
  val id: String,
  val from: String,
  val to: String,
  val type: String? = null,
  val properties: MutableMap<String, TShellValue> = mutableMapOf()
)

interface GraphStore {
  val rootId: String
  fun addNode(type: String, properties: Map<String, TShellValue> = emptyMap()): GraphNode
  fun addEdge(fromId: String, toId: String, type: String? = null, properties: Map<String, TShellValue> = emptyMap()): GraphEdge
  fun getNode(id: String): GraphNode?
  fun getEdge(id: String): GraphEdge?
  fun removeNode(id: String)
  fun removeEdge(id: String)
  fun outgoing(nodeId: String, edgeType: String? = null): List<GraphEdge>
  fun incoming(nodeId: String, edgeType: String? = null): List<GraphEdge>
  fun nodes(type: String? = null): List<GraphNode>
  fun edges(type: String? = null): List<GraphEdge>
  fun updateNode(id: String, properties: Map<String, TShellValue>)
  fun updateEdge(id: String, properties: Map<String, TShellValue>)

  /** Insert or update a node with an explicit ID. Used for merge/import/replication. */
  fun putNode(node: GraphNode)

  /** Insert or update an edge with an explicit ID. Used for merge/import/replication. */
  fun putEdge(edge: GraphEdge)
}
