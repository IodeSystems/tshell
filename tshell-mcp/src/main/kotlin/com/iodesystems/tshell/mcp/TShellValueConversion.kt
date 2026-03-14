package com.iodesystems.tshell.mcp

import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import kotlinx.serialization.json.*

/** Bidirectional conversion between TShellValue and kotlinx.serialization JsonElement. */
object TShellValueConversion {

  fun toJson(value: TShellValue): JsonElement = when (value) {
    is TString -> JsonPrimitive(value.value)
    is TNumber -> {
      val v = value.value
      if (v == v.toLong().toDouble()) JsonPrimitive(v.toLong())
      else JsonPrimitive(v)
    }
    is TBoolean -> JsonPrimitive(value.value)
    is TNull -> JsonNull
    is TArray -> JsonArray(value.elements.map { toJson(it) })
    is TObject -> JsonObject(value.fields.mapValues { toJson(it.value) })
    is TFunction -> JsonPrimitive(value.toDisplayString())
    is TRegex -> JsonPrimitive(value.toDisplayString())
  }

  fun fromJson(element: JsonElement): TShellValue = when (element) {
    is JsonNull -> TNull
    is JsonPrimitive -> when {
      element.isString -> TString(element.content)
      element.content == "true" -> TBoolean(true)
      element.content == "false" -> TBoolean(false)
      else -> TNumber(element.content.toDoubleOrNull() ?: 0.0)
    }
    is JsonArray -> TArray(element.map { fromJson(it) })
    is JsonObject -> TObject(element.mapValues { fromJson(it.value) })
  }

  /** Convert a list of TShellValue args to a JSON object map for MCP tool calls. */
  fun argsToJsonMap(
    args: List<TShellValue>,
    paramNames: List<String>
  ): Map<String, JsonElement> {
    // If single TObject arg, use its fields as named args
    if (args.size == 1 && args[0] is TObject) {
      return (args[0] as TObject).fields.mapValues { toJson(it.value) }
    }
    // Otherwise map positionally to param names
    return paramNames.zip(args).associate { (name, value) -> name to toJson(value) }
  }
}
