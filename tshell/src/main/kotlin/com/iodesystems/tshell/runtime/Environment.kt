package com.iodesystems.tshell.runtime

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Lexical scope with GIL-style concurrency.
 *
 * Reads ([get]) are lock-free via [ConcurrentHashMap].
 * Mutations ([define], [set], [defineOrSet], [remove], [restore]) acquire
 * a shared [ReentrantLock] (the "GIL") so compound operations like
 * check-then-write and parent-chain walks are atomic.
 *
 * The lock is shared across the entire environment graph — child scopes
 * inherit the same lock from their root. This allows `all()` branches
 * to execute in parallel coroutines while serializing state mutations.
 */
class Environment private constructor(
  private val parent: Environment?,
  private val gil: ReentrantLock
) {
  constructor(parent: Environment? = null) : this(parent, parent?.gil ?: ReentrantLock())

  private val bindings = ConcurrentHashMap<String, TShellValue>()

  fun get(name: String): TShellValue? =
    bindings[name] ?: parent?.get(name)

  fun define(name: String, value: TShellValue) = gil.withLock {
    if (bindings.containsKey(name)) {
      throw TShellError.runtime("'$name' is already declared in this scope")
    }
    bindings[name] = value
  }

  fun set(name: String, value: TShellValue) = gil.withLock {
    if (bindings.containsKey(name)) {
      bindings[name] = value
    } else if (parent != null) {
      parent.setLocked(name, value)
    } else {
      throw TShellError.runtime("'$name' is not defined")
    }
  }

  /** Internal: called when GIL is already held. Avoids recursive lock overhead. */
  private fun setLocked(name: String, value: TShellValue) {
    if (bindings.containsKey(name)) {
      bindings[name] = value
    } else if (parent != null) {
      parent.setLocked(name, value)
    } else {
      throw TShellError.runtime("'$name' is not defined")
    }
  }

  /**
   * Atomic read-modify-write for a value at the given path.
   * Reads the current value, applies the transform, and writes back — all under the GIL.
   * For nested paths (e.g. ["obj", "items"]), rebuilds the object chain.
   * Returns the new value.
   */
  fun mutate(path: List<String>, transform: (TShellValue) -> TShellValue): TShellValue = gil.withLock {
    if (path.isEmpty()) throw TShellError.runtime("mutate: empty path")
    if (path.size == 1) {
      val current = get(path[0]) ?: TShellValue.TNull
      val newVal = transform(current)
      setLocked(path[0], newVal)
      return@withLock newVal
    }
    // Nested path: read the chain, transform the leaf, rebuild and write back
    val root = get(path[0]) ?: throw TShellError.runtime("'${path[0]}' is not defined")
    val chain = mutableListOf(root)
    var current: TShellValue = root
    for (i in 1 until path.size) {
      current = when (current) {
        is TShellValue.TObject -> current.fields[path[i]] ?: TShellValue.TNull
        else -> throw TShellError.runtime("Cannot access '${path[i]}' on ${current.typeName()}")
      }
      chain.add(current)
    }
    // Transform the leaf
    val newLeaf = transform(chain.last())
    // Rebuild from inside out
    var updated: TShellValue = newLeaf
    for (i in path.size - 1 downTo 1) {
      val parent = chain[i - 1]
      updated = when (parent) {
        is TShellValue.TObject -> TShellValue.TObject(parent.fields + (path[i] to updated))
        else -> throw TShellError.runtime("Cannot set '${path[i]}' on ${parent.typeName()}")
      }
    }
    setLocked(path[0], updated)
    newLeaf
  }

  fun child(): Environment = Environment(this, gil)

  fun ownBindings(): Map<String, TShellValue> = bindings.toMap()

  fun allBindings(): Map<String, TShellValue> {
    val all = parent?.allBindings()?.toMutableMap() ?: mutableMapOf()
    all.putAll(bindings)
    return all
  }

  fun snapshot(): Map<String, TShellValue> = bindings.toMap()

  fun restore(state: Map<String, TShellValue>) = gil.withLock {
    bindings.clear()
    bindings.putAll(state)
  }

  fun remove(names: Set<String>) = gil.withLock {
    for (name in names) {
      bindings.remove(name)
    }
  }

  fun defineOrSet(name: String, value: TShellValue) = gil.withLock {
    bindings[name] = value
  }
}
