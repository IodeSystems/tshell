package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellValue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Discovers and loads tshell toolkit extensions from a directory.
 *
 * A toolkit is a subdirectory containing:
 *   - One or more .tshell files (function declarations become callable)
 *   - Optional typical_usage.md and/or advanced_usage.md (become help guide)
 *
 * Example layout:
 *   .tsh/
 *     math/
 *       stats.tshell
 *       linalg.tshell
 *       typical_usage.md
 *       advanced_usage.md
 *     text/
 *       nlp.tshell
 *       typical_usage.md
 */
class ToolkitLoader(private val dir: Path) {

  data class LoadResult(
    val name: String,
    val namespace: String?,
    val filesLoaded: Int,
    val functionsAdded: List<String>,
    val collisions: List<Collision>,
    val hasGuide: Boolean
  )

  data class Collision(
    val name: String,
    val owner: String
  ) {
    override fun toString() = "'$name' (already defined by $owner)"
  }

  // Tracks which names belong to which source (native command, toolkit, etc.)
  private val nameOwnership = mutableMapOf<String, String>()

  /**
   * Scans [dir] for subdirectories and loads each as a toolkit.
   *
   * @param namespaces map of toolkit directory name to namespace alias.
   *   e.g. mapOf("graph" to "g") makes graph functions accessible as g.addNode(...).
   *   Toolkits not in this map are loaded globally (flat).
   */
  fun loadAll(shell: TShell, namespaces: Map<String, String> = emptyMap()): List<LoadResult> {
    if (!dir.exists() || !dir.isDirectory()) return emptyList()

    // Snapshot existing names — native commands and globals from prior installs
    for (cmd in shell.commands.names()) {
      nameOwnership[cmd] = "native"
    }
    for (name in shell.getState().fields.keys) {
      nameOwnership.putIfAbsent(name, "native")
    }

    // Register the "toolkits" guide
    shell.registerGuide("toolkits", TOOLKITS_GUIDE)

    return Files.list(dir).use { stream ->
      stream
        .filter { Files.isDirectory(it) }
        .sorted()
        .map { loadToolkit(shell, it, namespaces[it.fileName.toString()]) }
        .toList()
    }
  }

  /**
   * Loads a single toolkit from the given directory.
   * Detects collisions against previously registered names.
   *
   * @param namespace if non-null, functions are wrapped in an object bound to this name
   *   instead of being registered as global functions. e.g. namespace="g" → g.myFn(...)
   */
  fun loadToolkit(shell: TShell, toolkitDir: Path, namespace: String? = null): LoadResult {
    val name = toolkitDir.fileName.toString()

    // Collect and sort .tshell files for deterministic load order
    val tshellFiles = Files.list(toolkitDir).use { stream ->
      stream
        .filter { it.extension == "tshell" && Files.isRegularFile(it) }
        .sorted()
        .toList()
    }

    // Parse function declarations from source before eval to detect collisions
    val declaredNames = mutableSetOf<String>()
    val sources = mutableListOf<String>()
    for (file in tshellFiles) {
      val source = file.readText()
      sources.add(source)
      FN_DECL_PATTERN.findAll(source).forEach { declaredNames.add(it.groupValues[1]) }
    }

    if (namespace != null) {
      // Namespaced loading: eval in isolated child env so globals aren't polluted.
      // Functions can still call each other since they share the child env's closure.
      val collisions = detectCollisions(namespace, name, isNamespace = true)

      // Eval all files in a shared isolated child environment
      val nsFunctions = shell.evalIsolated(sources)

      // Bind namespace object to globals
      shell.setState(mapOf(namespace to TShellValue.TObject(nsFunctions)))

      nameOwnership[namespace] = name

      // Build guide
      val guide = buildGuide(toolkitDir, name)
      if (guide != null) {
        shell.registerGuide(name, guide)
      }

      return LoadResult(name, namespace, tshellFiles.size, declaredNames.sorted(), collisions, guide != null)
    } else {
      // Flat loading: detect collisions per function name
      val collisions = detectCollisions(declaredNames, name)

      // Eval files
      for (source in sources) {
        shell.eval(source)
      }

      // Record ownership
      for (fn in declaredNames) {
        nameOwnership[fn] = name
      }

      // Build guide
      val guide = buildGuide(toolkitDir, name)
      if (guide != null) {
        shell.registerGuide(name, guide)
      }

      return LoadResult(name, null, tshellFiles.size, declaredNames.sorted(), collisions, guide != null)
    }
  }

  private fun detectCollisions(declaredNames: Set<String>, toolkitName: String): List<Collision> {
    val collisions = mutableListOf<Collision>()
    for (fn in declaredNames.sorted()) {
      val existingOwner = nameOwnership[fn]
      if (existingOwner != null) {
        collisions.add(Collision(fn, existingOwner))
      }
    }
    return collisions
  }

  private fun detectCollisions(namespace: String, toolkitName: String, isNamespace: Boolean): List<Collision> {
    val collisions = mutableListOf<Collision>()
    val existingOwner = nameOwnership[namespace]
    if (existingOwner != null) {
      collisions.add(Collision(namespace, existingOwner))
    }
    return collisions
  }

  private fun buildGuide(toolkitDir: Path, name: String): String? {
    val typical = toolkitDir.resolve("typical_usage.md")
    val advanced = toolkitDir.resolve("advanced_usage.md")

    val hasTypical = typical.exists() && typical.isRegularFile()
    val hasAdvanced = advanced.exists() && advanced.isRegularFile()

    if (!hasTypical && !hasAdvanced) return null

    return buildString {
      appendLine("$name toolkit")
      appendLine()
      if (hasTypical) {
        append(typical.readText().trimEnd())
        appendLine()
      }
      if (hasAdvanced) {
        if (hasTypical) appendLine()
        append(advanced.readText().trimEnd())
        appendLine()
      }
    }.trimEnd()
  }

  companion object {
    // Matches top-level function declarations: function name(...)
    private val FN_DECL_PATTERN = Regex("""(?:^|\n)\s*function\s+(\w+)\s*\(""")

    val TOOLKITS_GUIDE = """
Toolkit Extensions — packaging reusable tshell functions

A toolkit is a directory containing .tshell files and optional usage docs.
When loaded, all function declarations become globally callable (or namespaced).

DIRECTORY STRUCTURE:
  .tsh/                          # toolkit root (any directory)
    my-toolkit/                  # toolkit name = directory name
      01_base.tshell             # loaded in alphabetical order
      02_derived.tshell          # can reference fns from earlier files
      typical_usage.md           # optional: shown in help("my-toolkit")
      advanced_usage.md          # optional: appended to guide

FILE CONVENTIONS:
  - .tshell files are evaluated in alphabetical order within a toolkit
  - Prefix with numbers (01_, 02_) to control load order when files depend on each other
  - Non-.tshell files (except usage docs) are ignored
  - Toolkits are loaded in alphabetical order by directory name

USAGE DOCS:
  typical_usage.md               # common patterns, shown first in guide
  advanced_usage.md              # complex patterns, shown after typical

  These files are plain text. Use the same format as built-in guides:
    TYPICAL: Short description
      example_call()             // → expected result
      data |> pipe_call()         // → transformed result

    ADVANCED: Short description
      multi_step_example()

NAMESPACES:
  Toolkits can be loaded into a namespace to avoid collisions:

  From Kotlin host:
    shell.loadToolkits(Path.of(".tsh"), mapOf("graph" to "g"))
    // Now: g.addNode(...), g.out(...) instead of addNode(...), out(...)

  Namespaced toolkits bind all their functions to a single object:
    let g = graph      // conceptually — g.addNode(), g.out(), g.nodes()
    let m = math       // m.mean(), m.stddev()

  Functions within a namespaced toolkit can still call each other directly
  since they share the same closure scope during evaluation.

COLLISION DETECTION:
  When toolkits are loaded, name collisions are detected and reported.
  A collision occurs when two toolkits define a function with the same name,
  or when a toolkit function shadows a native command.
  The last-loaded definition wins, but collisions are flagged in LoadResult.
  Namespacing avoids collisions by isolating toolkit functions behind a prefix.

LOADING:
  From Kotlin host:
    shell.loadToolkits(Path.of(".tsh"))                          // load all, flat
    shell.loadToolkits(Path.of(".tsh"), mapOf("math" to "m"))    // namespace math as m
    shell.loadToolkit(Path.of(".tsh/my-toolkit"))                 // load one, flat
    shell.loadToolkit(Path.of(".tsh/my-toolkit"), namespace="m")  // load one, namespaced

  From tshell (if FileToolkit is installed):
    load(".tsh/my-toolkit/helpers.tshell")         // load individual file (always flat)

EXAMPLE typical_usage.md:
  TYPICAL: Compute statistics
    mean([1, 2, 3, 4, 5])       // → 3
    median([1, 2, 3, 4, 5])     // → 3
    stddev([2, 4, 4, 4, 5])     // → 1

  TYPICAL: Combine with pipes
    data |> map(r => r.score) |> mean()
    """.trimIndent()
  }
}
