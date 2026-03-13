package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.*
import org.testng.annotations.Test
import com.iodesystems.tshell.assertThrows
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import java.nio.file.Path
import kotlin.io.path.*

class ToolkitLoaderTest {

  lateinit var tmp: Path

  @BeforeMethod
  fun createTempDir() { tmp = java.nio.file.Files.createTempDirectory("test") }

  @AfterMethod
  fun deleteTempDir() { tmp.toFile().deleteRecursively() }

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  // --- Basic loading ---

  @Test fun `loads tshell files from toolkit directory`() {
    val toolkitDir = tmp.resolve("math")
    toolkitDir.createDirectories()
    toolkitDir.resolve("helpers.tshell").writeText("""
      function square(x) { return x * x }
      function cube(x) { return x * x * x }
    """.trimIndent())

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(TNumber(25.0), sh.eval("square(5)"))
    assertEquals(TNumber(125.0), sh.eval("cube(5)"))
    assertEquals(listOf("cube", "square"), results[0].functionsAdded)
  }

  @Test fun `multiple toolkits loaded in alphabetical order`() {
    for (name in listOf("zz_last", "aa_first", "mm_middle")) {
      val dir = tmp.resolve(name)
      dir.createDirectories()
      dir.resolve("main.tshell").writeText("""function ${name}_fn() { return "$name" }""")
    }

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(3, results.size)
    assertEquals("aa_first", results[0].name)
    assertEquals("mm_middle", results[1].name)
    assertEquals("zz_last", results[2].name)

    assertEquals(TString("aa_first"), sh.eval("aa_first_fn()"))
  }

  @Test fun `tshell files within toolkit loaded in alphabetical order`() {
    val toolkitDir = tmp.resolve("ordered")
    toolkitDir.createDirectories()
    toolkitDir.resolve("01_base.tshell").writeText("function base_val() { return 10 }")
    toolkitDir.resolve("02_derived.tshell").writeText("function derived_val() { return base_val() + 5 }")

    val sh = shell()
    sh.loadToolkits(tmp)

    assertEquals(TNumber(15.0), sh.eval("derived_val()"))
  }

  @Test fun `loadToolkit loads single toolkit`() {
    val toolkitDir = tmp.resolve("single")
    toolkitDir.createDirectories()
    toolkitDir.resolve("fn.tshell").writeText("function single_fn() { return 42 }")

    val sh = shell()
    val result = sh.loadToolkit(toolkitDir)

    assertEquals("single", result.name)
    assertEquals(TNumber(42.0), sh.eval("single_fn()"))
  }

  @Test fun `toolkit functions compose with core commands`() {
    val toolkitDir = tmp.resolve("compose")
    toolkitDir.createDirectories()
    toolkitDir.resolve("fns.tshell").writeText("""
      function increment(x) { return x + 1 }
      function isPositive(x) { return x > 0 }
    """.trimIndent())

    val sh = shell()
    sh.loadToolkits(tmp)

    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(3.0), TNumber(4.0))),
      sh.eval("[1, 2, 3] |> map(increment)")
    )
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(2.0))),
      sh.eval("[-1, 0, 1, 2] |> filter(isPositive)")
    )
  }

  @Test fun `non-tshell files in toolkit directory are ignored`() {
    val toolkitDir = tmp.resolve("mixed")
    toolkitDir.createDirectories()
    toolkitDir.resolve("real.tshell").writeText("function real_fn() { return true }")
    toolkitDir.resolve("notes.txt").writeText("this is not a tshell file")
    toolkitDir.resolve("README.md").writeText("# Mixed toolkit")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(1, results[0].filesLoaded)
    assertEquals(TBoolean(true), sh.eval("real_fn()"))
  }

  @Test fun `empty directory returns empty results`() {
    val sh = shell()
    val results = sh.loadToolkits(tmp)
    assertEquals(0, results.size)
  }

  @Test fun `nonexistent directory returns empty results`() {
    val sh = shell()
    val results = sh.loadToolkits(tmp.resolve("does_not_exist"))
    assertEquals(0, results.size)
  }

  // --- Guides ---

  @Test fun `registers guide from usage markdown files`() {
    val toolkitDir = tmp.resolve("math")
    toolkitDir.createDirectories()
    toolkitDir.resolve("helpers.tshell").writeText("function double(x) { return x * 2 }")
    toolkitDir.resolve("typical_usage.md").writeText("""
      TYPICAL: Basic math
        double(5)  // → 10
    """.trimIndent())
    toolkitDir.resolve("advanced_usage.md").writeText("""
      ADVANCED: Composed math
        [1, 2, 3] |> map(double)  // → [2, 4, 6]
    """.trimIndent())

    val sh = shell()
    sh.loadToolkits(tmp)

    val helpText = (sh.eval("""help("math")""") as TString).value
    assertTrue(helpText.contains("math toolkit"))
    assertTrue(helpText.contains("TYPICAL"))
    assertTrue(helpText.contains("ADVANCED"))
  }

  @Test fun `guide with only typical usage`() {
    val toolkitDir = tmp.resolve("utils")
    toolkitDir.createDirectories()
    toolkitDir.resolve("fns.tshell").writeText("function noop() { return null }")
    toolkitDir.resolve("typical_usage.md").writeText("TYPICAL: Just the basics\n  noop()")

    val sh = shell()
    sh.loadToolkits(tmp)

    val help = (sh.eval("""help("utils")""") as TString).value
    assertTrue(help.contains("TYPICAL"))
  }

  @Test fun `no guide registered when no markdown files`() {
    val toolkitDir = tmp.resolve("bare")
    toolkitDir.createDirectories()
    toolkitDir.resolve("a.tshell").writeText("function bare_fn() { return 1 }")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(1, results.size)
    assertEquals("bare", results[0].name)
    assertEquals(1, results[0].filesLoaded)
    assertFalse(results[0].hasGuide)

    assertEquals(TNumber(1.0), sh.eval("bare_fn()"))
  }

  @Test fun `toolkits guide is registered on loadAll`() {
    // Even with no toolkits, the "toolkits" guide is registered
    tmp.resolve("empty_tk").createDirectories()
    tmp.resolve("empty_tk/a.tshell").writeText("function etk_fn() { return 1 }")

    val sh = shell()
    sh.loadToolkits(tmp)

    val help = (sh.eval("""help("toolkits")""") as TString).value
    assertTrue(help.contains("DIRECTORY STRUCTURE"))
    assertTrue(help.contains("typical_usage.md"))
    assertTrue(help.contains("COLLISION DETECTION"))
  }

  // --- Collision detection ---

  @Test fun `detects collision between toolkits`() {
    val tk1 = tmp.resolve("alpha")
    tk1.createDirectories()
    tk1.resolve("fns.tshell").writeText("function shared_fn() { return 1 }")

    val tk2 = tmp.resolve("beta")
    tk2.createDirectories()
    tk2.resolve("fns.tshell").writeText("function shared_fn() { return 2 }")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    // alpha loads first, no collision
    assertEquals(0, results[0].collisions.size)
    // beta collides with alpha's shared_fn
    assertEquals(1, results[1].collisions.size)
    assertEquals("shared_fn", results[1].collisions[0].name)
    assertEquals("alpha", results[1].collisions[0].owner)

    // Last loaded wins
    assertEquals(TNumber(2.0), sh.eval("shared_fn()"))
  }

  @Test fun `detects collision with native command`() {
    val tk = tmp.resolve("overrides")
    tk.createDirectories()
    // "len" is a native CoreToolkit command
    tk.resolve("fns.tshell").writeText("function len() { return 999 }")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(1, results[0].collisions.size)
    assertEquals("len", results[0].collisions[0].name)
    assertEquals("native", results[0].collisions[0].owner)
  }

  @Test fun `no collisions when names are unique`() {
    val tk1 = tmp.resolve("aaa")
    tk1.createDirectories()
    tk1.resolve("fns.tshell").writeText("function unique_a() { return 1 }")

    val tk2 = tmp.resolve("bbb")
    tk2.createDirectories()
    tk2.resolve("fns.tshell").writeText("function unique_b() { return 2 }")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(0, results[0].collisions.size)
    assertEquals(0, results[1].collisions.size)
  }

  @Test fun `functionsAdded tracks what each toolkit introduced`() {
    val tk1 = tmp.resolve("aaa")
    tk1.createDirectories()
    tk1.resolve("fns.tshell").writeText("""
      function fn_a() { return 1 }
      function fn_b() { return 2 }
    """.trimIndent())

    val tk2 = tmp.resolve("bbb")
    tk2.createDirectories()
    tk2.resolve("fns.tshell").writeText("function fn_c() { return 3 }")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertEquals(listOf("fn_a", "fn_b"), results[0].functionsAdded)
    assertEquals(listOf("fn_c"), results[1].functionsAdded)
  }

  @Test fun `collision toString is descriptive`() {
    val collision = ToolkitLoader.Collision("myFn", "other-toolkit")
    assertEquals("'myFn' (already defined by other-toolkit)", collision.toString())
  }

  // --- Namespaces ---

  @Test fun `namespaced toolkit binds functions to object`() {
    val tk = tmp.resolve("math")
    tk.createDirectories()
    tk.resolve("fns.tshell").writeText("""
      function square(x) { return x * x }
      function cube(x) { return x * x * x }
    """.trimIndent())

    val sh = shell()
    sh.loadToolkits(tmp, mapOf("math" to "m"))

    assertEquals(TNumber(25.0), sh.eval("m.square(5)"))
    assertEquals(TNumber(125.0), sh.eval("m.cube(5)"))
  }

  @Test fun `namespaced functions not available as globals`() {
    val tk = tmp.resolve("math")
    tk.createDirectories()
    tk.resolve("fns.tshell").writeText("function ns_only() { return 42 }")

    val sh = shell()
    sh.loadToolkits(tmp, mapOf("math" to "m"))

    assertEquals(TNumber(42.0), sh.eval("m.ns_only()"))
    assertThrows<Exception> { sh.eval("ns_only()") }
  }

  @Test fun `namespace avoids collision with flat toolkit`() {
    val tk1 = tmp.resolve("alpha")
    tk1.createDirectories()
    tk1.resolve("fns.tshell").writeText("function compute() { return 1 }")

    val tk2 = tmp.resolve("beta")
    tk2.createDirectories()
    tk2.resolve("fns.tshell").writeText("function compute() { return 2 }")

    val sh = shell()
    // alpha is flat, beta is namespaced — no collision
    val results = sh.loadToolkits(tmp, mapOf("beta" to "b"))

    assertEquals(0, results[0].collisions.size) // alpha: flat, no collision
    assertEquals(0, results[1].collisions.size) // beta: namespaced as "b", no collision
    assertEquals(TNumber(1.0), sh.eval("compute()"))    // alpha's flat version
    assertEquals(TNumber(2.0), sh.eval("b.compute()"))  // beta's namespaced version
  }

  @Test fun `namespace collision detected when alias shadows existing name`() {
    val tk1 = tmp.resolve("alpha")
    tk1.createDirectories()
    tk1.resolve("fns.tshell").writeText("function m() { return 1 }")

    val tk2 = tmp.resolve("beta")
    tk2.createDirectories()
    tk2.resolve("fns.tshell").writeText("function compute() { return 2 }")

    val sh = shell()
    // alpha defines "m" as a global fn, beta namespaced as "m" would collide
    val results = sh.loadToolkits(tmp, mapOf("beta" to "m"))

    assertEquals(0, results[0].collisions.size)
    assertEquals(1, results[1].collisions.size)
    assertEquals("m", results[1].collisions[0].name)
    assertEquals("alpha", results[1].collisions[0].owner)
  }

  @Test fun `namespace result includes namespace field`() {
    val tk = tmp.resolve("math")
    tk.createDirectories()
    tk.resolve("fns.tshell").writeText("function add(a, b) { return a + b }")

    val sh = shell()
    val results = sh.loadToolkits(tmp, mapOf("math" to "m"))

    assertEquals("m", results[0].namespace)
  }

  @Test fun `flat toolkit result has null namespace`() {
    val tk = tmp.resolve("math")
    tk.createDirectories()
    tk.resolve("fns.tshell").writeText("function add(a, b) { return a + b }")

    val sh = shell()
    val results = sh.loadToolkits(tmp)

    assertNull(results[0].namespace)
  }

  @Test fun `namespaced toolkit with multiple files`() {
    val tk = tmp.resolve("utils")
    tk.createDirectories()
    tk.resolve("01_base.tshell").writeText("function helper() { return 10 }")
    tk.resolve("02_main.tshell").writeText("function compute() { return helper() + 5 }")

    val sh = shell()
    sh.loadToolkits(tmp, mapOf("utils" to "u"))

    assertEquals(TNumber(15.0), sh.eval("u.compute()"))
    assertEquals(TNumber(10.0), sh.eval("u.helper()"))
  }

  @Test fun `loadToolkit with namespace parameter`() {
    val tk = tmp.resolve("single")
    tk.createDirectories()
    tk.resolve("fns.tshell").writeText("function greet() { return \"hi\" }")

    val sh = shell()
    val result = sh.loadToolkit(tk, namespace = "s")

    assertEquals("s", result.namespace)
    assertEquals(TString("hi"), sh.eval("s.greet()"))
  }
}
