package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import kotlin.io.path.*
import kotlin.math.max
import kotlin.math.min

class FileToolkit(
  private val root: Path,
  private val readOnly: Boolean = false,
  private val maxReadLines: Int = 200
) {

  init {
    require(root.isAbsolute) { "FileToolkit root must be an absolute path" }
    require(root.exists()) { "FileToolkit root does not exist: $root" }
  }

  fun install(shell: TShell): TShell {

    shell.registerGuide("files", """
File Toolkit — ${if (readOnly) "read and search files (read-only mode)" else "read, write, search, and edit files"}

TYPICAL: Read and inspect files
  read("config.json")                    // full file with line numbers (1: content)
  read("big.log", 100, 20)              // 20 lines starting at line 100
  readLines("data.csv") |> len()         // count lines
  exists("output.txt")                  // → true/false
  ls("src") |> filter(f => f.type == "file") |> map(f => f.name)

TYPICAL: Search for files and content
  glob("src/**/*.kt")                   // find files by pattern (skips hidden/build dirs)
  glob("**/*.ts", {grep: "TODO"})       // find files + grep content
  // → [{file: "app.ts", num: 42, line: "// TODO fix this"}]
  glob("*", {dirs: true})               // include directories in results
  glob("**/*", {depth: 2})              // limit traversal depth
  glob("**/*", {hidden: true})          // include hidden files (.git, .gradle, etc)

  grep("app.ts", {match: "import", context: 2})
  // → [{line: "import ...", num: 1, context: [...]}]
  grep("app.ts", {match: "TODO", mode: "count"})      // → 3
  grep("app.ts", {match: "TODO", mode: "files"})       // → true
  grep("app.ts", {match: "TODO", limit: 2})            // first 2 matches only

TYPICAL: Write and edit files
  write("output.txt", "hello world")
  append("log.txt", "new entry\n")
  edit("app.ts", "const x = 1", "const x = 42")
  edit("app.ts", "var ", "const ", {all: true})  // replace all occurrences

TYPICAL: File management
  mkdir("output/reports")
  mv("old.txt", "new.txt")
  delete("temp.txt")

ADVANCED: Bulk search and transform
  // Find all TODO comments across the codebase
  glob("src/**/*.kt", {grep: "TODO"})
    |> map(hit => hit.file + ":" + str(hit.num) + " " + hit.line |> trim())

  // Find large files
  ls("src") |> filter(f => f.type == "file" && f.size > 10000) |> sort("size") |> reverse()

ADVANCED: Multi-step file processing
  // Read CSV, process, write result
  let lines = readLines("data.csv") |> skip(1)  // skip header
  let parsed = lines |> map(line => {
    let parts = line |> split(",")
    {name: parts[0], value: num(parts[1])}
  })
  let total = parsed |> map(r => r.value) |> reduce((s, v) => s + v, 0)
  write("result.txt", "Total: " + str(total))

ADVANCED: Search and replace across files
  let files = glob("src/**/*.ts", {grep: "oldFunction"})
  let affected = files |> map(hit => hit.file) |> unique()
  for (let f of affected) {
    edit(f, "oldFunction", "newFunction", {all: true})
  }
""".trimIndent())

    shell.register(
      "glob", "pattern: string, opts?: {grep?: string, depth?: number, dirs?: boolean, hidden?: boolean}",
      "finds files matching glob pattern. Skips hidden (dot) dirs/files and build dirs by default. " +
        "Use {hidden: true} to include hidden, {dirs: true} to include directories, {depth: N} to limit depth, " +
        "{grep: pattern} to search content returning {file, num, line} hits",
      listOf(
        """glob("src/**/*.kt")""",
        """glob("**/*.ts", {grep: "TODO"})""",
        """glob("*", {dirs: true})""",
        """glob("**/*.log", {depth: 3})""",
      )
    ) { args ->
      val pattern = requireString("glob", args, 0)
      val opts = args.getOrElse(1) { TNull } as? TObject
      val grepPattern = (opts?.fields?.get("grep") as? TString)?.value
      val maxDepth = (opts?.fields?.get("depth") as? TNumber)?.value?.toInt()
      val includeDirs = (opts?.fields?.get("dirs") as? TBoolean)?.value ?: false
      val includeHidden = (opts?.fields?.get("hidden") as? TBoolean)?.value ?: false

      val matcher = globMatcher(pattern)
      val matchedFiles = mutableListOf<Path>()
      val walker = if (maxDepth != null) Files.walk(root, maxDepth) else Files.walk(root)
      walker.use { stream ->
        stream.forEach { path ->
          if (path == root) return@forEach
          val rel = root.relativize(path)
          // Skip hidden files/dirs unless requested
          if (!includeHidden && rel.any { it.toString().startsWith(".") }) return@forEach
          // Skip build directories unless hidden is enabled
          if (!includeHidden && rel.first().toString() == "build") return@forEach
          val isDir = Files.isDirectory(path)
          if (isDir && !includeDirs) return@forEach
          if (!isDir && !Files.isRegularFile(path)) return@forEach
          if (matcher.matches(rel)) {
            matchedFiles.add(path)
          }
        }
      }
      matchedFiles.sortBy { root.relativize(it).toString() }

      if (grepPattern != null) {
        val regex = Regex(grepPattern)
        val results = mutableListOf<TShellValue>()
        for (path in matchedFiles) {
          if (Files.isDirectory(path)) continue
          val rel = root.relativize(path).toString()
          val lines = try { path.readLines() } catch (_: Exception) { continue }
          for ((idx, line) in lines.withIndex()) {
            if (regex.containsMatchIn(line)) {
              results.add(TObject(mapOf(
                "file" to TString(rel),
                "num" to TNumber((idx + 1).toDouble()),
                "line" to TString(line)
              )))
            }
          }
        }
        TArray(results)
      } else {
        TArray(matchedFiles.map { TString(root.relativize(it).toString()) })
      }
    }

    shell.register(
      "read", "path: string, start?: number, lines?: number",
      "reads file with line numbers (1: content). Optional start line (1-based) and line count for partial reads. " +
        "Large files are automatically truncated to ${maxReadLines} lines — use start/lines params to read specific sections",
      listOf("""read("config.json")""", """read("big.log", 100, 20)""")
    ) { args ->
      val path = requireString("read", args, 0)
      val start = (args.getOrElse(1) { TNull } as? TNumber)?.value?.toInt()
      val lineCount = (args.getOrElse(2) { TNull } as? TNumber)?.value?.toInt()
      val resolved = confine(path)
      if (!resolved.exists()) {
        throw TShellError("read: file not found '$path'\n\n  Resolved to: $resolved")
      }
      val allLines = resolved.readLines()
      val totalLines = allLines.size
      val width = totalLines.toString().length

      fun numbered(lines: List<String>, startNum: Int): String =
        lines.mapIndexed { i, line ->
          val num = startNum + i
          "${num.toString().padStart(width)}: $line"
        }.joinToString("\n")

      if (start != null) {
        val startIdx = (start - 1).coerceAtLeast(0)
        val count = lineCount ?: (totalLines - startIdx)
        val slice = allLines.drop(startIdx).take(count)
        TString(numbered(slice, startIdx + 1))
      } else if (totalLines > maxReadLines) {
        val slice = allLines.take(maxReadLines)
        TString(numbered(slice, 1) + "\n\n... truncated (showing $maxReadLines of $totalLines lines). " +
          "Use read(\"$path\", ${maxReadLines + 1}) to continue.")
      } else {
        TString(numbered(allLines, 1))
      }
    }

    shell.register(
      "readLines", "path: string",
      "reads file as array of lines",
      listOf("""readLines("data.txt") |> filter(line => line |> len() > 0)""")
    ) { args ->
      val path = requireString("readLines", args, 0)
      val resolved = confine(path)
      if (!resolved.exists()) {
        throw TShellError("readLines: file not found '$path'\n\n  Resolved to: $resolved")
      }
      TArray(resolved.readLines().map { TString(it) })
    }

    if (!readOnly) {
    shell.register(
      "write", "path: string, content: string",
      "writes content to file, creates parent directories",
      listOf("""write("out.txt", "hello")""")
    ) { args ->
      val path = requireString("write", args, 0)
      val content = requireString("write", args, 1)
      val resolved = confine(path)
      resolved.parent?.createDirectories()
      resolved.writeText(content)
      TNull
    }
    } // end write-only: write

    shell.register(
      "exists", "path: string",
      "checks if file or directory exists",
      listOf("""exists("config.json")""")
    ) { args ->
      val path = requireString("exists", args, 0)
      TBoolean(confine(path).exists())
    }

    shell.register(
      "ls", "path?: string",
      "lists directory contents as array of {name, type, size}",
      listOf("""ls()""", """ls("src")""")
    ) { args ->
      val path = (args.firstOrNull() as? TString)?.value ?: ""
      val resolved = confine(path)
      if (!resolved.exists() || !resolved.isDirectory()) {
        throw TShellError("ls: not a directory '$path'\n\n  Resolved to: $resolved")
      }
      val entries = Files.list(resolved).use { stream ->
        stream.sorted().map { entry ->
          val rel = root.relativize(entry)
          TObject(mapOf(
            "name" to TString(rel.toString()),
            "type" to TString(if (entry.isDirectory()) "dir" else "file"),
            "size" to TNumber(if (entry.isRegularFile()) entry.fileSize().toDouble() else 0.0)
          ))
        }.toList()
      }
      TArray(entries)
    }

    shell.register(
      "tree", "path?: string, opts?: {depth?: number, files?: boolean}",
      "shows directory tree. Default: dirs with file counts, depth 4. " +
        "Use {files: true} to list files with line counts. " +
        "Collapses single-child chains. Skips hidden/build dirs",
      listOf("""tree()""", """tree("src")""", """tree("src", {depth: 6, files: true})""")
    ) { args ->
      val path = (args.getOrElse(0) { TNull } as? TString)?.value ?: ""
      val opts = args.getOrElse(1) { TNull } as? TObject
      val maxDepth = (opts?.fields?.get("depth") as? TNumber)?.value?.toInt() ?: 4
      val showFiles = (opts?.fields?.get("files") as? TBoolean)?.value ?: false
      val resolved = confine(path)
      if (!resolved.exists() || !resolved.isDirectory()) {
        throw TShellError("tree: not a directory '$path'\n\n  Resolved to: $resolved")
      }
      fun filteredChildren(dir: Path): List<Path> = Files.list(dir).use { stream ->
        stream.sorted().filter { child ->
          val name = child.fileName.toString()
          !name.startsWith(".") && name != "build"
        }.toList()
      }
      fun lineCount(file: Path): Int = try { Files.readAllLines(file).size } catch (_: Exception) { 0 }

      val sb = StringBuilder()
      fun walk(dir: Path, prefix: String, depth: Int) {
        if (depth > maxDepth) {
          sb.appendLine("${prefix}...")
          return
        }
        val allChildren = filteredChildren(dir)
        val dirs = allChildren.filter { it.isDirectory() }
        val files = allChildren.filter { it.isRegularFile() }

        // Collapse single-child dir chains: a/ -> b/ -> c/ becomes a/b/c/
        if (dirs.size == 1 && files.isEmpty()) {
          val child = dirs[0]
          val chainParts = mutableListOf(child.fileName.toString())
          var current = child
          while (true) {
            val grandChildren = filteredChildren(current)
            val subDirs = grandChildren.filter { it.isDirectory() }
            val subFiles = grandChildren.filter { it.isRegularFile() }
            if (subDirs.size == 1 && subFiles.isEmpty()) {
              chainParts.add(subDirs[0].fileName.toString())
              current = subDirs[0]
            } else break
          }
          sb.appendLine("$prefix└── ${chainParts.joinToString("/")}/" )
          walk(current, "$prefix    ", depth + 1)
          return
        }

        if (showFiles) {
          val items = dirs + files
          for ((i, child) in items.withIndex()) {
            val isLast = i == items.size - 1
            val connector = if (isLast) "└── " else "├── "
            val name = child.fileName.toString()
            if (child.isDirectory()) {
              sb.appendLine("$prefix$connector$name/")
              walk(child, prefix + if (isLast) "    " else "│   ", depth + 1)
            } else {
              val lines = lineCount(child)
              sb.appendLine("$prefix$connector$name ($lines lines)")
            }
          }
        } else {
          val allItems = dirs.size + if (files.isNotEmpty()) 1 else 0
          var idx = 0
          for (d in dirs) {
            idx++
            val isLast = idx == allItems
            val connector = if (isLast) "└── " else "├── "
            sb.appendLine("$prefix$connector${d.fileName}/")
            walk(d, prefix + if (isLast) "    " else "│   ", depth + 1)
          }
          if (files.isNotEmpty()) {
            idx++
            val isLast = idx == allItems
            val connector = if (isLast) "└── " else "├── "
            val exts = files.groupBy { it.fileName.toString().substringAfterLast('.', "") }
              .map { (ext, list) ->
                val totalLines = list.sumOf { lineCount(it) }
                if (ext.isEmpty()) "${list.size} files" else "${list.size} .${ext} (${totalLines}L)"
              }.joinToString(", ")
            sb.appendLine("$prefix$connector[$exts]")
          }
        }
      }
      val rootName = if (path.isEmpty()) "." else path
      sb.appendLine("$rootName/")
      walk(resolved, "", 1)
      TString(sb.toString().trimEnd())
    }

    shell.register(
      "grep", "path: string, opts: {match: string, context?: number, mode?: string, limit?: number}",
      "searches file for pattern. Default returns [{line, num}]. " +
        "mode: \"count\" returns match count. mode: \"files\" returns true/false. " +
        "context: N adds surrounding lines. limit: N caps results",
      listOf(
        """grep("app.ts", {match: "TODO"})""",
        """grep("app.ts", {match: "TODO", mode: "count"})""",
        """grep("app.ts", "import")""",
        """glob("**/*.kt") |> map(f => {file: f, hits: grep(f, {match: "test", context: 2, limit: 5})})""",
      )
    ) { args ->
      val path = requireString("grep", args, 0)
      val opts = args.getOrElse(1) { TNull }
      val match: String
      val contextLines: Int
      val mode: String
      val limit: Int
      when (opts) {
        is TObject -> {
          match = (opts.fields["match"] as? TString)?.value
            ?: throw TShellError.wrongArguments("grep", "path, {match: string, context?: number, mode?: string, limit?: number}", args,
              """grep("file.txt", {match: "pattern"})""")
          contextLines = (opts.fields["context"] as? TNumber)?.value?.toInt() ?: 0
          mode = (opts.fields["mode"] as? TString)?.value ?: "content"
          limit = (opts.fields["limit"] as? TNumber)?.value?.toInt() ?: Int.MAX_VALUE
        }
        is TString -> {
          match = opts.value
          contextLines = 0
          mode = "content"
          limit = Int.MAX_VALUE
        }
        else -> throw TShellError.wrongArguments("grep", "path, {match: string}", args,
          """grep("file.txt", {match: "pattern"})""")
      }

      val resolved = confine(path)
      if (!resolved.exists()) {
        throw TShellError("grep: file not found '$path'\n\n  Resolved to: $resolved")
      }

      val allLines = resolved.readLines()
      val regex = Regex(match)

      when (mode) {
        "count" -> {
          val count = allLines.count { regex.containsMatchIn(it) }
          TNumber(count.toDouble())
        }
        "files" -> {
          TBoolean(allLines.any { regex.containsMatchIn(it) })
        }
        else -> {
          val results = mutableListOf<TShellValue>()
          for ((idx, line) in allLines.withIndex()) {
            if (results.size >= limit) break
            if (regex.containsMatchIn(line)) {
              val contextStart = max(0, idx - contextLines)
              val contextEnd = min(allLines.size - 1, idx + contextLines)
              val ctx = if (contextLines > 0) {
                TArray((contextStart..contextEnd).map { i ->
                  TObject(mapOf(
                    "num" to TNumber((i + 1).toDouble()),
                    "line" to TString(allLines[i]),
                    "match" to TBoolean(i == idx)
                  ))
                })
              } else TNull

              val entry = mutableMapOf<String, TShellValue>(
                "line" to TString(line),
                "num" to TNumber((idx + 1).toDouble())
              )
              if (ctx != TNull) entry["context"] = ctx
              results.add(TObject(entry))
            }
          }
          TArray(results)
        }
      }
    }

    if (!readOnly) {
    shell.register(
      "append", "path: string, content: string",
      "appends content to file",
      listOf("""append("log.txt", "entry\\n")""")
    ) { args ->
      val path = requireString("append", args, 0)
      val content = requireString("append", args, 1)
      val resolved = confine(path)
      resolved.parent?.createDirectories()
      resolved.toFile().appendText(content)
      TNull
    }

    shell.register(
      "edit", "path: string, old: string, new: string, opts?: {all?: boolean}",
      "replaces exact string match in file. Fails if not found. Fails if ambiguous unless all=true",
      listOf(
        """edit("app.ts", "const x = 1", "const x = 42")""",
        """edit("app.ts", "var ", "const ", {all: true})""",
      )
    ) { args ->
      val path = requireString("edit", args, 0)
      val old = requireString("edit", args, 1)
      val new = requireString("edit", args, 2)
      val opts = args.getOrElse(3) { TNull }
      val all = (opts as? TObject)?.fields?.get("all")?.let { (it as? TBoolean)?.value } ?: false
      val resolved = confine(path)
      if (!resolved.exists()) {
        throw TShellError("edit: file not found '$path'\n\n  Resolved to: $resolved")
      }
      val content = resolved.readText()
      val count = content.windowed(old.length, 1).count { it == old }
      when {
        count == 0 -> throw TShellError(
          "edit: old string not found in '$path'\n\n" +
            "  Searched for:\n    ${old.lines().joinToString("\n    ")}\n\n" +
            "  Hint: the old string must match exactly, including whitespace and indentation"
        )
        count > 1 && !all -> throw TShellError(
          "edit: old string appears $count times in '$path'\n\n" +
            "  Provide more surrounding context to make the match unique, or use {all: true}"
        )
      }
      resolved.writeText(if (all) content.replace(old, new) else content.replaceFirst(old, new))
      TNull
    }

    shell.register(
      "delete", "path: string",
      "deletes a file or empty directory",
      listOf("""delete("temp.txt")""")
    ) { args ->
      val path = requireString("delete", args, 0)
      val resolved = confine(path)
      if (!resolved.exists()) {
        throw TShellError("delete: file not found '$path'\n\n  Resolved to: $resolved")
      }
      Files.delete(resolved)
      TNull
    }

    shell.register(
      "mv", "from: string, to: string",
      "moves/renames a file or directory",
      listOf("""mv("old.txt", "new.txt")""", """mv("src/old.ts", "src/renamed.ts")""")
    ) { args ->
      val from = requireString("mv", args, 0)
      val to = requireString("mv", args, 1)
      val resolvedFrom = confine(from)
      val resolvedTo = confine(to)
      if (!resolvedFrom.exists()) {
        throw TShellError("mv: source not found '$from'\n\n  Resolved to: $resolvedFrom")
      }
      resolvedTo.parent?.createDirectories()
      Files.move(resolvedFrom, resolvedTo)
      TNull
    }

    shell.register(
      "mkdir", "path: string",
      "creates directory and any parent directories",
      listOf("""mkdir("output/reports")""")
    ) { args ->
      val path = requireString("mkdir", args, 0)
      val resolved = confine(path)
      resolved.createDirectories()
      TNull
    }

    shell.register(
      "load", "path: string",
      "loads and evaluates a .tshell file, making its definitions available",
      listOf("""load("lib/helpers.tshell")""", """load(".tshell/customFn.tshell")""")
    ) { args ->
      val path = requireString("load", args, 0)
      val resolved = confine(path)
      if (!resolved.exists()) {
        throw TShellError("load: file not found '$path'\n\n  Resolved to: $resolved")
      }
      val source = resolved.readText()
      shell.eval(source)
    }

    } // end if (!readOnly)

    return shell
  }

  // --- Path confinement ---

  private fun confine(path: String): Path {
    val resolved = root.resolve(path).normalize().toAbsolutePath()
    if (!resolved.startsWith(root)) {
      throw TShellError(
        "Access denied: '$path' resolves outside the allowed directory\n\n" +
          "  Root: $root\n" +
          "  Resolved: $resolved\n\n" +
          "  All file operations are confined to the root directory."
      )
    }
    return resolved
  }

  private fun globMatcher(pattern: String): PathMatcher {
    return FileSystems.getDefault().getPathMatcher("glob:$pattern")
  }

  private fun requireString(cmd: String, args: List<TShellValue>, idx: Int): String {
    return (args.getOrElse(idx) { TNull } as? TString)?.value
      ?: throw TShellError.wrongArguments(cmd, "string", args)
  }
}
