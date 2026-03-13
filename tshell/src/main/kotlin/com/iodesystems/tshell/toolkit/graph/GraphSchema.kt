package com.iodesystems.tshell.toolkit.graph

import com.iodesystems.tshell.runtime.TShellError

data class NodeSchema(
  val required: Set<String> = emptySet(),
  val optional: Set<String>? = null // null = any extra props allowed
)

data class EdgeSchema(
  val from: String? = null,
  val to: String? = null
)

data class GraphSchema(
  val nodes: Map<String, NodeSchema> = emptyMap(),
  val edges: Map<String, EdgeSchema> = emptyMap(),
  val strict: Boolean = false // if true, reject unknown node/edge types
) {

  fun validateNode(type: String, props: Map<String, *>) {
    if (strict && type !in nodes) {
      throw TShellError(
        "Schema violation: unknown node type '$type'\n\n" +
          "  Allowed types: ${nodes.keys.sorted().joinToString(", ")}"
      )
    }
    val schema = nodes[type] ?: return
    val missing = schema.required - props.keys
    if (missing.isNotEmpty()) {
      throw TShellError(
        "Schema violation: '$type' node missing required properties: ${missing.sorted().joinToString(", ")}\n\n" +
          "  Required: ${schema.required.sorted().joinToString(", ")}"
      )
    }
    if (schema.optional != null) {
      val allowed = schema.required + schema.optional + setOf("id", "type")
      val extra = props.keys - allowed
      if (extra.isNotEmpty()) {
        throw TShellError(
          "Schema violation: '$type' node has unknown properties: ${extra.sorted().joinToString(", ")}\n\n" +
            "  Allowed: ${(schema.required + schema.optional).sorted().joinToString(", ")}"
        )
      }
    }
  }

  fun validateEdge(edgeType: String?, fromType: String, toType: String) {
    if (edgeType == null) return
    if (strict && edgeType !in edges) {
      throw TShellError(
        "Schema violation: unknown edge type '$edgeType'\n\n" +
          "  Allowed types: ${edges.keys.sorted().joinToString(", ")}"
      )
    }
    val schema = edges[edgeType] ?: return
    if (schema.from != null && fromType != schema.from) {
      throw TShellError(
        "Schema violation: '$edgeType' edge must originate from '${schema.from}', got '$fromType'"
      )
    }
    if (schema.to != null && toType != schema.to) {
      throw TShellError(
        "Schema violation: '$edgeType' edge must target '${schema.to}', got '$toType'"
      )
    }
  }

  fun toGuide(): String = buildString {
    if (nodes.isNotEmpty()) {
      appendLine("SCHEMA: Node types")
      for ((type, schema) in nodes.toSortedMap()) {
        val parts = mutableListOf<String>()
        if (schema.required.isNotEmpty()) parts.add("required: ${schema.required.sorted().joinToString(", ")}")
        if (schema.optional != null) parts.add("optional: ${schema.optional.sorted().joinToString(", ")}")
        appendLine("  $type — ${parts.joinToString("; ").ifEmpty { "no constraints" }}")
      }
    }
    if (edges.isNotEmpty()) {
      if (nodes.isNotEmpty()) appendLine()
      appendLine("SCHEMA: Edge types")
      for ((type, schema) in edges.toSortedMap()) {
        val parts = mutableListOf<String>()
        if (schema.from != null) parts.add("from: ${schema.from}")
        if (schema.to != null) parts.add("to: ${schema.to}")
        appendLine("  $type — ${parts.joinToString(", ").ifEmpty { "no constraints" }}")
      }
    }
    if (strict) {
      appendLine()
      appendLine("Schema is strict: only listed node and edge types are allowed.")
    }
  }.trimEnd()
}
