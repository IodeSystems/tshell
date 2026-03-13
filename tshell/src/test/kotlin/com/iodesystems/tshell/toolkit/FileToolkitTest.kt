package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

class FileToolkitTest {

  @TempDir
  lateinit var tempDir: Path

  private lateinit var shell: TShell

  @BeforeEach
  fun setup() {
    // Copy test-project fixture into tempDir
    val fixture = Path.of("src/test/resources/test-project")
    fixture.toFile().walkTopDown().forEach { file ->
      val rel = fixture.toFile().toPath().relativize(file.toPath())
      val target = tempDir.resolve(rel)
      if (file.isDirectory) {
        target.createDirectories()
      } else {
        target.parent?.createDirectories()
        file.copyTo(target.toFile())
      }
    }

    shell = TShell()
    CoreToolkit.install(shell)
    FileToolkit(tempDir).install(shell)
  }

  // --- Basic file operations ---

  @Test fun `read file`() {
    val result = shell.eval("""read("config/settings.json")""") as TString
    assertTrue(result.value.contains("test-project"))
    assertTrue(result.value.contains("3000"))
  }

  @Test fun `read missing file gives helpful error`() {
    val err = assertThrows<TShellError> { shell.eval("""read("nope.txt")""") }
    assertTrue(err.message!!.contains("file not found"))
  }

  @Test fun `readLines returns array of lines`() {
    val result = shell.eval("""readLines("config/routes.csv")""") as TArray
    assertEquals(7, result.elements.size) // header + 6 routes
    assertTrue((result.elements[0] as TString).value.contains("method"))
  }

  @Test fun `write and read back`() {
    shell.eval("""write("output/result.txt", "generated content")""")
    val result = shell.eval("""read("output/result.txt")""") as TString
    assertTrue(result.value.contains("1: generated content"))
  }

  @Test fun `write creates parent directories`() {
    shell.eval("""write("deep/nested/dir/file.txt", "deep")""")
    val result = shell.eval("""read("deep/nested/dir/file.txt")""") as TString
    assertTrue(result.value.contains("1: deep"))
  }

  @Test fun `append to file`() {
    shell.eval("""write("log.txt", "line1\n")""")
    shell.eval("""append("log.txt", "line2\n")""")
    val result = shell.eval("""read("log.txt")""") as TString
    assertTrue(result.value.contains("line1"))
    assertTrue(result.value.contains("line2"))
  }

  @Test fun `exists checks files and directories`() {
    assertEquals(TBoolean(true), shell.eval("""exists("config/settings.json")"""))
    assertEquals(TBoolean(true), shell.eval("""exists("src/main")"""))
    assertEquals(TBoolean(false), shell.eval("""exists("nope.txt")"""))
  }

  // --- Path confinement ---

  @Test fun `path traversal with dots is blocked`() {
    val err = assertThrows<TShellError> { shell.eval("""read("../../etc/passwd")""") }
    assertTrue(err.message!!.contains("Access denied"))
    assertTrue(err.message!!.contains("outside the allowed directory"))
  }

  @Test fun `absolute path outside root is blocked`() {
    val err = assertThrows<TShellError> { shell.eval("""read("/etc/passwd")""") }
    assertTrue(err.message!!.contains("Access denied"))
  }

  @Test fun `write outside root is blocked`() {
    val err = assertThrows<TShellError> { shell.eval("""write("../escape.txt", "bad")""") }
    assertTrue(err.message!!.contains("Access denied"))
  }

  // --- Glob ---

  @Test fun `glob finds typescript files`() {
    val result = shell.eval("""glob("src/**/*.ts")""") as TArray
    assertEquals(5, result.elements.size)
  }

  @Test fun `glob finds by extension`() {
    val result = shell.eval("""glob("**/*.json")""") as TArray
    assertEquals(1, result.elements.size)
    assertTrue((result.elements[0] as TString).value.contains("settings.json"))
  }

  @Test fun `glob finds files in specific directory`() {
    val result = shell.eval("""glob("src/main/*.ts")""") as TArray
    assertEquals(3, result.elements.size)
  }

  @Test fun `glob returns empty for no matches`() {
    val result = shell.eval("""glob("**/*.rs")""") as TArray
    assertEquals(0, result.elements.size)
  }

  @Test fun `glob results are sorted`() {
    val result = shell.eval("""glob("src/**/*.ts")""") as TArray
    val names = result.elements.map { (it as TString).value }
    assertEquals(names.sorted(), names)
  }

  // --- ls ---

  @Test fun `ls root lists top-level entries`() {
    val result = shell.eval("""ls()""") as TArray
    val names = result.elements.map { ((it as TObject).fields["name"] as TString).value }
    assertTrue(names.any { it.contains("src") })
    assertTrue(names.any { it.contains("config") })
    assertTrue(names.any { it.contains("docs") })
  }

  @Test fun `ls subdirectory`() {
    val result = shell.eval("""ls("src/main")""") as TArray
    assertEquals(3, result.elements.size)
    val first = result.elements[0] as TObject
    assertTrue(first.fields.containsKey("name"))
    assertTrue(first.fields.containsKey("type"))
    assertTrue(first.fields.containsKey("size"))
  }

  @Test fun `ls shows type correctly`() {
    val result = shell.eval("""ls("src")""") as TArray
    val types = result.elements.associate {
      val obj = it as TObject
      val name = (obj.fields["name"] as TString).value
      val type = (obj.fields["type"] as TString).value
      name.substringAfterLast("/") to type
    }
    assertEquals("dir", types["main"])
    assertEquals("dir", types["util"])
  }

  @Test fun `ls nonexistent directory fails`() {
    val err = assertThrows<TShellError> { shell.eval("""ls("nonexistent")""") }
    assertTrue(err.message!!.contains("not a directory"))
  }

  // --- Grep ---

  @Test fun `grep finds pattern matches`() {
    val result = shell.eval("""grep("src/main/app.ts", {match: "TODO"})""") as TArray
    assertEquals(3, result.elements.size)
    val first = result.elements[0] as TObject
    assertTrue(first.fields.containsKey("line"))
    assertTrue(first.fields.containsKey("num"))
  }

  @Test fun `grep with context returns surrounding lines`() {
    val result = shell.eval("""grep("src/main/app.ts", {match: "TODO", context: 1})""") as TArray
    assertEquals(3, result.elements.size)
    val first = result.elements[0] as TObject
    val ctx = first.fields["context"] as TArray
    assertTrue(ctx.elements.size >= 2) // at least match + one context line
    // Context entries have num, line, match fields
    val ctxEntry = ctx.elements[0] as TObject
    assertTrue(ctxEntry.fields.containsKey("num"))
    assertTrue(ctxEntry.fields.containsKey("line"))
    assertTrue(ctxEntry.fields.containsKey("match"))
  }

  @Test fun `grep with simple string pattern`() {
    val result = shell.eval("""grep("src/main/app.ts", "console")""") as TArray
    assertEquals(1, result.elements.size)
  }

  @Test fun `grep with regex pattern`() {
    val result = shell.eval("""grep("src/main/models.ts", {match: "export (interface|function)"})""") as TArray
    assertTrue(result.elements.size >= 3) // User, Post, Comment interfaces + functions
  }

  @Test fun `grep returns line numbers correctly`() {
    val result = shell.eval("""grep("config/routes.csv", {match: "POST"})""") as TArray
    assertEquals(2, result.elements.size) // two POST routes
    val nums = result.elements.map { ((it as TObject).fields["num"] as TNumber).value.toInt() }
    assertTrue(nums.all { it > 1 }) // not the header
  }

  @Test fun `grep count mode returns number`() {
    val result = shell.eval("""grep("src/main/app.ts", {match: "TODO", mode: "count"})""")
    assertEquals(TNumber(3.0), result)
  }

  @Test fun `grep files mode returns boolean`() {
    assertEquals(TBoolean(true), shell.eval("""grep("src/main/app.ts", {match: "TODO", mode: "files"})"""))
    assertEquals(TBoolean(false), shell.eval("""grep("src/main/app.ts", {match: "ZZZNOMATCH", mode: "files"})"""))
  }

  @Test fun `grep with limit caps results`() {
    val result = shell.eval("""grep("src/main/app.ts", {match: "TODO", limit: 1})""") as TArray
    assertEquals(1, result.elements.size)
  }

  @Test fun `grep missing file fails`() {
    val err = assertThrows<TShellError> { shell.eval("""grep("nope.txt", {match: "x"})""") }
    assertTrue(err.message!!.contains("file not found"))
  }

  // --- Composition: realistic scenarios ---

  @Test fun `find all TODOs across project`() {
    val result = shell.eval("""
      glob("**/*.ts")
        |> map(f => {file: f, todos: grep(f, {match: "TODO"})})
        |> filter(x => x.todos |> len() > 0)
    """) as TArray
    // app.ts has 3 TODOs, models.ts has 1, db.ts has 3
    assertEquals(3, result.elements.size)
    val totalTodos = result.elements.sumOf {
      ((it as TObject).fields["todos"] as TArray).elements.size
    }
    assertEquals(7, totalTodos)
  }

  @Test fun `find TODOs with context and limit`() {
    val result = shell.eval("""
      glob("**/*.ts")
        |> map(f => {file: f, todos: grep(f, {match: "TODO", context: 1}) |> limit(2)})
        |> filter(x => x.todos |> len() > 0)
    """) as TArray
    // Files with TODOs are limited to 2 hits each
    result.elements.forEach { entry ->
      val todos = ((entry as TObject).fields["todos"] as TArray)
      assertTrue(todos.elements.size <= 2)
    }
  }

  @Test fun `count lines per file`() {
    val result = shell.eval("""
      glob("src/**/*.ts")
        |> map(f => {file: f, lines: readLines(f) |> len()})
        |> sort("lines")
        |> reverse()
    """) as TArray
    assertEquals(5, result.elements.size)
    // Verify sorted descending by line count
    val counts = result.elements.map { ((it as TObject).fields["lines"] as TNumber).value }
    assertEquals(counts.sortedDescending(), counts)
  }

  @Test fun `find files containing specific imports`() {
    val result = shell.eval("""
      glob("src/**/*.ts")
        |> filter(f => (grep(f, {match: "import"}) |> len()) > 0)
    """) as TArray
    // Only app.ts has imports
    assertEquals(1, result.elements.size)
    assertTrue((result.elements[0] as TString).value.contains("app.ts"))
  }

  @Test fun `extract route info from csv`() {
    val result = shell.eval("""
      readLines("config/routes.csv")
        |> skip(1)
        |> map(line => line |> split(","))
        |> filter(parts => parts[3] == "true")
        |> map(parts => {method: parts[0], path: parts[1]})
    """) as TArray
    // Routes with auth=true: GET /users, GET /users/:id, POST /users, POST /posts
    assertEquals(4, result.elements.size)
  }

  @Test fun `generate summary report`() {
    val result = shell.eval("""
      let files = glob("src/**/*.ts")
      let report = {
        totalFiles: files |> len(),
        totalLines: files |> map(f => readLines(f) |> len()) |> reduce((a, b) => a + b, 0),
        todoCount: files |> map(f => grep(f, {match: "TODO"}) |> len()) |> reduce((a, b) => a + b, 0)
      }
      report
    """)
    val report = result as TObject
    assertEquals(TNumber(5.0), report.fields["totalFiles"])
    assertTrue((report.fields["totalLines"] as TNumber).value > 0)
    assertEquals(TNumber(7.0), report.fields["todoCount"])
  }

  @Test fun `write transformed output`() {
    shell.eval("""
      let todos = glob("**/*.ts")
        |> map(f => {file: f, todos: grep(f, {match: "TODO"})})
        |> filter(x => x.todos |> len() > 0)
        |> map(x => x.file + ": " + str(x.todos |> len()) + " TODOs")
        |> join("\n")
      write("todo-report.txt", todos)
    """)
    val report = shell.eval("""read("todo-report.txt")""") as TString
    assertTrue(report.value.contains("app.ts"))
    assertTrue(report.value.contains("TODO"))
  }

  @Test fun `empty directory glob returns empty`() {
    val result = shell.eval("""glob("empty-dir/*")""") as TArray
    assertEquals(0, result.elements.size)
  }

  @Test fun `read dotfile`() {
    val result = shell.eval("""read(".gitignore")""") as TString
    assertTrue(result.value.contains("node_modules"))
  }

  // --- load ---

  @Test fun `load tshell file makes definitions available`() {
    shell.eval("""load(".tshell/helpers.tshell")""")
    assertEquals(TNumber(84.0), shell.eval("double(42)"))
    assertEquals(TBoolean(true), shell.eval("isEven(4)"))
    assertEquals(TBoolean(false), shell.eval("isEven(3)"))
    assertEquals(TString("hello world"), shell.eval("""greet("world")"""))
    assertEquals(TNumber(10.0), shell.eval("DEFAULT_LIMIT"))
  }

  @Test fun `load tshell file with file operations`() {
    shell.eval("""load(".tshell/analysis.tshell")""")
    val result = shell.eval("todoReport()") as TArray
    assertEquals(3, result.elements.size) // 3 files with TODOs
  }

  @Test fun `loaded fn compositions work with pipes`() {
    shell.eval("""load(".tshell/helpers.tshell")""")
    val result = shell.eval("[1, 2, 3, 4] |> filter(isEven) |> map(double)")
    assertEquals(TArray(listOf(TNumber(4.0), TNumber(8.0))), result)
  }

  @Test fun `load missing file fails`() {
    val err = assertThrows<TShellError> { shell.eval("""load("nope.tshell")""") }
    assertTrue(err.message!!.contains("file not found"))
  }

  @Test fun `load respects path confinement`() {
    val err = assertThrows<TShellError> { shell.eval("""load("../../escape.tshell")""") }
    assertTrue(err.message!!.contains("Access denied"))
  }

  // --- edit ---

  @Test fun `edit replaces exact match`() {
    shell.eval("""write("test.txt", "hello world")""")
    shell.eval("""edit("test.txt", "world", "tshell")""")
    val result = shell.eval("""read("test.txt")""") as TString
    assertTrue(result.value.contains("hello tshell"))
  }

  @Test fun `edit fails on missing string`() {
    shell.eval("""write("test.txt", "hello world")""")
    val err = assertThrows<TShellError> { shell.eval("""edit("test.txt", "nothere", "x")""") }
    assertTrue(err.message!!.contains("not found"))
  }

  @Test fun `edit fails on ambiguous match`() {
    shell.eval("""write("test.txt", "aaa bbb aaa")""")
    val err = assertThrows<TShellError> { shell.eval("""edit("test.txt", "aaa", "x")""") }
    assertTrue(err.message!!.contains("2 times"))
    assertTrue(err.message!!.contains("{all: true}"))
  }

  @Test fun `edit preserves multiline context`() {
    shell.eval("""write("test.txt", "line1\nline2\nline3")""")
    shell.eval("""edit("test.txt", "line2", "replaced")""")
    val result = shell.eval("""read("test.txt")""") as TString
    assertTrue(result.value.contains("1: line1"))
    assertTrue(result.value.contains("2: replaced"))
    assertTrue(result.value.contains("3: line3"))
  }

  @Test fun `edit missing file fails`() {
    val err = assertThrows<TShellError> { shell.eval("""edit("nope.txt", "a", "b")""") }
    assertTrue(err.message!!.contains("file not found"))
  }

  @Test fun `edit all replaces all occurrences`() {
    shell.eval("""write("test.txt", "aaa bbb aaa")""")
    shell.eval("""edit("test.txt", "aaa", "x", {all: true})""")
    val result = shell.eval("""read("test.txt")""") as TString
    assertTrue(result.value.contains("x bbb x"))
  }

  @Test fun `edit all fails on missing string`() {
    shell.eval("""write("test.txt", "hello")""")
    val err = assertThrows<TShellError> { shell.eval("""edit("test.txt", "nope", "x", {all: true})""") }
    assertTrue(err.message!!.contains("not found"))
  }

  // --- read with offset ---

  @Test fun `read with start line`() {
    shell.eval("""write("test.txt", "line1\nline2\nline3\nline4\nline5")""")
    val result = shell.eval("""read("test.txt", 3)""") as TString
    assertTrue(result.value.contains("3: line3"))
    assertTrue(result.value.contains("4: line4"))
    assertTrue(result.value.contains("5: line5"))
    assertFalse(result.value.contains("line1"))
    assertFalse(result.value.contains("line2"))
  }

  @Test fun `read with start and line count`() {
    shell.eval("""write("test.txt", "line1\nline2\nline3\nline4\nline5")""")
    val result = shell.eval("""read("test.txt", 2, 2)""") as TString
    assertTrue(result.value.contains("2: line2"))
    assertTrue(result.value.contains("3: line3"))
    assertFalse(result.value.contains("line4"))
  }

  @Test fun `read full file without offset`() {
    val result = shell.eval("""read("config/settings.json")""") as TString
    assertTrue(result.value.contains("test-project"))
  }

  // --- glob with grep ---

  @Test fun `glob with grep returns file line num`() {
    val result = shell.eval("""glob("**/*.ts", {grep: "TODO"})""") as TArray
    assertEquals(7, result.elements.size) // 7 total TODOs across files
    val first = result.elements[0] as TObject
    assertTrue(first.fields.containsKey("file"))
    assertTrue(first.fields.containsKey("num"))
    assertTrue(first.fields.containsKey("line"))
  }

  @Test fun `glob with grep filters to matching files`() {
    val result = shell.eval("""glob("src/main/*.ts", {grep: "import"})""") as TArray
    // Only app.ts has imports
    assertTrue(result.elements.all { ((it as TObject).fields["file"] as TString).value.contains("app.ts") })
  }

  @Test fun `glob with grep no matches returns empty`() {
    val result = shell.eval("""glob("**/*.ts", {grep: "ZZZNOMATCH"})""") as TArray
    assertEquals(0, result.elements.size)
  }

  @Test fun `glob without grep still returns file paths`() {
    val result = shell.eval("""glob("src/**/*.ts")""") as TArray
    assertEquals(5, result.elements.size)
    assertTrue(result.elements[0] is TString)
  }

  // --- delete ---

  @Test fun `delete removes file`() {
    shell.eval("""write("temp.txt", "content")""")
    assertEquals(TBoolean(true), shell.eval("""exists("temp.txt")"""))
    shell.eval("""delete("temp.txt")""")
    assertEquals(TBoolean(false), shell.eval("""exists("temp.txt")"""))
  }

  @Test fun `delete missing file fails`() {
    val err = assertThrows<TShellError> { shell.eval("""delete("nope.txt")""") }
    assertTrue(err.message!!.contains("file not found"))
  }

  @Test fun `delete respects confinement`() {
    val err = assertThrows<TShellError> { shell.eval("""delete("../../escape.txt")""") }
    assertTrue(err.message!!.contains("Access denied"))
  }

  // --- mv ---

  @Test fun `mv renames file`() {
    shell.eval("""write("old.txt", "content")""")
    shell.eval("""mv("old.txt", "new.txt")""")
    assertEquals(TBoolean(false), shell.eval("""exists("old.txt")"""))
    val result = shell.eval("""read("new.txt")""") as TString
    assertTrue(result.value.contains("content"))
  }

  @Test fun `mv moves to subdirectory`() {
    shell.eval("""write("file.txt", "data")""")
    shell.eval("""mv("file.txt", "moved/file.txt")""")
    val result = shell.eval("""read("moved/file.txt")""") as TString
    assertTrue(result.value.contains("data"))
  }

  @Test fun `mv missing source fails`() {
    val err = assertThrows<TShellError> { shell.eval("""mv("nope.txt", "dest.txt")""") }
    assertTrue(err.message!!.contains("source not found"))
  }

  // --- tree ---

  @Test fun `tree shows directory structure`() {
    val result = shell.eval("""tree()""") as TString
    assertTrue(result.value.contains("src/"))
    assertTrue(result.value.contains("config/"))
    // Hidden dirs should not appear
    assertFalse(result.value.contains(".tshell"))
    assertFalse(result.value.contains(".gitignore"))
  }

  @Test fun `tree with files mode shows line counts`() {
    val result = shell.eval("""tree("src", {files: true})""") as TString
    assertTrue(result.value.contains("app.ts"))
    assertTrue(result.value.contains("lines"))
  }

  @Test fun `tree compact mode shows file extension summary`() {
    val result = shell.eval("""tree("src")""") as TString
    assertTrue(result.value.contains(".ts"))
    assertTrue(result.value.contains("L)"))
  }

  @Test fun `tree collapses single-child chains`() {
    // Create a deep single-child chain: deep/a/b/c/file.txt
    val deep = tempDir.resolve("deep/a/b/c")
    Files.createDirectories(deep)
    Files.writeString(deep.resolve("file.txt"), "content")
    val result = shell.eval("""tree("deep")""") as TString
    // Should collapse a/b/c into one line
    assertTrue(result.value.contains("a/b/c/"), "Expected collapsed chain, got:\n${result.value}")
    // Should NOT have separate entries for a/, b/, c/
    assertFalse(result.value.contains("└── a/\n"), "Should not have separate 'a/' entry")
  }

  @Test fun `tree respects depth limit`() {
    val result = shell.eval("""tree(".", {depth: 1})""") as TString
    // Should show top-level dirs but not recurse deeply
    assertTrue(result.value.contains("src/"))
    // At depth 1, children of src should show "..." not recurse further
    assertTrue(result.value.contains("..."))
  }

  // --- mkdir ---

  @Test fun `mkdir creates directory`() {
    shell.eval("""mkdir("new-dir")""")
    assertEquals(TBoolean(true), shell.eval("""exists("new-dir")"""))
  }

  @Test fun `mkdir creates nested directories`() {
    shell.eval("""mkdir("deep/nested/dir")""")
    assertEquals(TBoolean(true), shell.eval("""exists("deep/nested/dir")"""))
  }

  @Test fun `mkdir then write into it`() {
    shell.eval("""mkdir("out")""")
    shell.eval("""write("out/result.txt", "done")""")
    val result = shell.eval("""read("out/result.txt")""") as TString
    assertTrue(result.value.contains("done"))
  }
}

class FileToolkitReadOnlyTest {

  @TempDir
  lateinit var tempDir: Path

  @Test fun `read-only mode registers tree and grep but not write`() {
    val fixture = Path.of("src/test/resources/test-project")
    fixture.toFile().walkTopDown().forEach { file ->
      val rel = fixture.toFile().toPath().relativize(file.toPath())
      val target = tempDir.resolve(rel)
      if (file.isDirectory) target.createDirectories()
      else { target.parent?.createDirectories(); file.copyTo(target.toFile()) }
    }
    val sh = TShell()
    CoreToolkit.install(sh)
    FileToolkit(tempDir, readOnly = true).install(sh)
    // tree should work
    val tree = sh.eval("""tree()""") as TString
    assertTrue(tree.value.contains("src/"))
    // write should not be registered
    val help = sh.eval("""help()""") as TString
    assertFalse(help.value.contains("write("))
  }
}
