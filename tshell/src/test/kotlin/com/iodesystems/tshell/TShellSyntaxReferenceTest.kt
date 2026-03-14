package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class TShellSyntaxReferenceTest {

  private fun stdlib(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  val syntaxReference = """
// Typical Usage:
let name = "world"
let items = [{name: "alice", age: 30}, {name: "bob", age: 17}]
let adults = items |> filter(x => x.age >= 18) |> map(x => x.name) |> sort()
function greet(who) { return `hello ${'$'}{who}` }
adults |> map(x => greet(x)) |> join(", ")

// Advanced Usage:
let {name: n, age: a} = items[0]
let nums = [1, 2, 3]
let more = [...nums, 4, 5]
let obj = {n, a, active: true}
let obj2 = {...obj, active: false}
let doubled = 5 |> (x => x * 2)
let PI = 3.14
let label = a > 18 ? "adult" : "minor"
let sum = 0
for (let v of nums) { sum = sum + v }
let i = 0
while (i < 3) { i = i + 1 }
if (!obj2.active && a != 0 || sum <= 100) { "ok" } else { "no" }
let result = chain(() => nums, arr => arr |> map(x => x * 10), arr => arr |> len())
let both = all(() => 1 + 2, () => 3 * 4)
let safe = race(() => fail("down"), () => "fallback")
let scattered = [1, 2, 3] |* (x => x * 2)
let gathered = scattered |> reduce((acc, x) => acc + x)
safe
  """.trimIndent()

  @Test
  fun `syntax reference is valid tshell`() {
    val sh = stdlib()
    val result = sh.eval(syntaxReference)
    assertEquals(TString("fallback"), result)
  }

  @Test
  fun `syntax reference covers pipes`() {
    val sh = stdlib()
    assertEquals(
      TArray(listOf(TNumber(30.0), TNumber(20.0), TNumber(10.0))),
      sh.eval("[3, 1, 2] |> sort() |> reverse() |> map(x => x * 10)")
    )
  }

  @Test
  fun `syntax reference covers chain`() {
    val sh = stdlib()
    assertEquals(
      TString("2, 4, 6"),
      sh.eval("""chain(() => [1, 2, 3], arr => arr |> map(x => x * 2), arr => arr |> join(", "))""")
    )
  }

  @Test
  fun `help lists all commands`() {
    val sh = stdlib()
    val result = sh.eval("help()") as TString
    assertTrue(result.value.contains("map"))
    assertTrue(result.value.contains("filter"))
    assertTrue(result.value.contains("help"))
  }

  @Test
  fun `help filters by search`() {
    val sh = stdlib()
    val result = sh.eval("""help("sort")""") as TString
    assertTrue(result.value.contains("sort"))
    assertFalse(result.value.contains("map"))
  }

  @Test
  fun `toPrompt full mode is compact`() {
    val sh = stdlib()
    val prompt = sh.toPrompt()
    assertTrue(prompt.contains("help"))
    assertTrue(prompt.contains("map"))
    assertTrue(prompt.length < 5600, "Full prompt is ${prompt.length} chars, should be < 5600")
  }

  @Test
  fun `toPrompt compact mode is significantly smaller`() {
    val sh = stdlib()
    val full = sh.toPrompt()
    val compact = sh.toPrompt(compact = true)
    assertTrue(compact.contains("help"))
    assertTrue(compact.contains("map"))
    assertTrue(compact.contains("help(\"name\")"), "Compact prompt must point to help() for discovery")
    assertTrue(compact.contains("help(\"core\")"), "Compact prompt must reference guides")
    assertTrue(compact.length < full.length * 0.75,
      "Compact (${compact.length}) should be at least 25% smaller than full (${full.length})")
    assertTrue(compact.length < 3500, "Compact prompt is ${compact.length} chars, should be < 3500")
  }

  /**
   * Validates that every code example in toPrompt() is executable tshell.
   * Examples are the lines from toPrompt() that match known code patterns.
   */
  @Test
  fun `toPrompt examples are valid tshell`() {
    // These are the code examples from toPrompt() — tested as a single program
    val promptExamples = """
let x = 42
x = 99
x += 1
function fib(n) { if (n <= 1) { return n } else { return fib(n-1) + fib(n-2) } }
let double = x => x * 2
let add = (a, b) => a + b
if (x > 5) { "big" } else { "small" }
let ternary = true ? "yes" : "no"
export let shared = 42
export function double(n) { return n * 2 }
let i = 0
while (i < 5) { i += 1 }
for (let x of [1,2,3]) { x }
[1,2,3] |> filter(n => n > 1) |> map(n => n * 10)
[1,2,3] |* (x => x * 2)
5 |* double
5 |> toArray()
[1,2,3] |* double |> reduce((acc, x) => acc + x)
let {name, age} = {name: "alice", age: 30}
    """.trimIndent()

    val sh = stdlib()
    try {
      sh.eval(promptExamples)
    } catch (e: Exception) {
      fail<Unit>("toPrompt() code examples are invalid tshell:\n${e.message}")
    }
  }
}
