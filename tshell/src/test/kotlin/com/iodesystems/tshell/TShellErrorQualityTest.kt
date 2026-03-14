package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class TShellErrorQualityTest {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  private fun errorMessage(code: String): String {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval(code) }
    return err.message!!
  }

  // --- Parse errors should be helpful ---

  @Test fun `missing expression after let`() {
    val msg = errorMessage("let x =")
    println("ERROR: $msg")
    assertTrue(msg.contains("let x ="), "Should show source")
    assertTrue(msg.contains("^"), "Should show pointer")
  }

  @Test fun `missing parens in if`() {
    val msg = errorMessage("if x > 1 { }")
    println("ERROR: $msg")
  }

  @Test fun `unclosed paren`() {
    val msg = errorMessage("function foo( { }")
    println("ERROR: $msg")
  }

  @Test fun `unclosed bracket`() {
    val msg = errorMessage("[1, 2,")
    println("ERROR: $msg")
  }

  @Test fun `missing value in object`() {
    val msg = errorMessage("let x = {a: }")
    println("ERROR: $msg")
  }

  @Test fun `dangling dot`() {
    val msg = errorMessage("let x = {a: 1}; x.")
    println("ERROR: $msg")
  }

  @Test fun `missing identifier after let`() {
    val msg = errorMessage("let = 5")
    println("ERROR: $msg")
  }

  @Test fun `for without let or const`() {
    val msg = errorMessage("for (x of [1]) { }")
    println("ERROR: $msg")
  }

  // --- help() ---

  @Test fun `help with specific command shows full docs`() {
    val sh = shell()
    val result = sh.eval("""help("map")""") as TString
    println("HELP MAP:\n${result.value}")
    assertTrue(result.value.contains("map"))
    assertTrue(result.value.contains("applies fn"))
  }

  @Test fun `help with search filters`() {
    val sh = shell()
    val result = sh.eval("""help("string")""") as TString
    println("HELP STRING:\n${result.value}")
  }

  @Test fun `help lists available guides`() {
    val sh = shell()
    val result = sh.eval("help()") as TString
    assertTrue(result.value.contains("Guides:"))
    assertTrue(result.value.contains("core"))
  }

  @Test fun `help shows guide content`() {
    val sh = shell()
    val result = sh.eval("""help("core")""") as TString
    assertTrue(result.value.contains("TYPICAL"))
    assertTrue(result.value.contains("ADVANCED"))
  }

  // --- Runtime errors ---

  @Test fun `unknown command suggests similar`() {
    val msg = errorMessage("mpa([1], x => x)")
    assertTrue(msg.contains("Did you mean"))
    assertTrue(msg.contains("map"))
  }

  @Test fun `wrong arg type shows expected and got`() {
    val msg = errorMessage("[1,2] |> map(42)")
    assertTrue(msg.contains("function"))
  }

  @Test fun `pipe type mismatch shows what happened`() {
    val msg = errorMessage("42 |> map(x => x)")
    assertTrue(msg.contains("array"))
    assertTrue(msg.contains("number"))
  }

  // --- JS compatibility guidance ---

  @Test fun `var works as let alias`() {
    val sh = shell()
    assertEquals(TShellValue.TNumber(5.0), sh.eval("var x = 5; x"))
  }

  @Test fun `console dot log resolves to print`() {
    val sh = shell()
    assertEquals(TShellValue.TString("hi"), sh.eval("""console.log("hi")"""))
  }

  @Test fun `JSON dot parse resolves to parseJson`() {
    val sh = shell()
    assertEquals(TShellValue.TObject(mapOf("a" to TShellValue.TNumber(1.0))), sh.eval("""JSON.parse('{"a":1}')"""))
  }

  @Test fun `JSON dot stringify resolves to toJson`() {
    val sh = shell()
    assertEquals(TShellValue.TString("""{"a":1}"""), sh.eval("""JSON.stringify({a: 1})"""))
  }

  @Test fun `Math dot floor resolves to floor`() {
    val sh = shell()
    assertEquals(TShellValue.TNumber(3.0), sh.eval("Math.floor(3.5)"))
  }

  @Test fun `Object dot keys resolves to keys`() {
    val sh = shell()
    assertEquals(TShellValue.TArray(listOf(TShellValue.TString("a"))), sh.eval("Object.keys({a: 1})"))
  }

  @Test fun `new suggests no classes`() {
    val msg = errorMessage("new Date()")
    assertTrue(msg.contains("no constructors"), msg)
  }

  @Test fun `class suggests objects and functions`() {
    val msg = errorMessage("class Foo {}")
    assertTrue(msg.contains("objects and functions"), msg)
  }

  @Test fun `throw works`() {
    val sh = shell()
    val msg = errorMessage("""throw "something broke"""")
    assertTrue(msg.contains("something broke"), msg)
  }

  @Test fun `String constructor resolves to str`() {
    val sh = shell()
    assertEquals(TShellValue.TString("42"), sh.eval("String(42)"))
  }

  @Test fun `Number constructor resolves to num`() {
    val sh = shell()
    assertEquals(TShellValue.TNumber(42.0), sh.eval("""Number("42")"""))
  }

  // --- Method syntax now auto-resolves to commands ---

  @Test fun `array method map works`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(4.0), TNumber(6.0))),
      sh.eval("[1,2,3].map(x => x * 2)")
    )
  }

  @Test fun `string method toUpperCase works`() {
    val sh = shell()
    assertEquals(TString("HELLO"), sh.eval(""""hello".toUpperCase()"""))
  }

  @Test fun `string method split works`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TString("a"), TString("b"))),
      sh.eval(""""a,b".split(",")""")
    )
  }

  @Test fun `array method filter works`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(3.0))),
      sh.eval("[1,2,3].filter(x => x > 1)")
    )
  }

  @Test fun `array method includes resolves to contains`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("[1,2,3].includes(2)"))
  }

  @Test fun `string method includes resolves to contains`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval(""""hello world".includes("world")"""))
  }

  @Test fun `string method toLowerCase works`() {
    val sh = shell()
    assertEquals(TString("hello"), sh.eval(""""HELLO".toLowerCase()"""))
  }

  @Test fun `method syntax as partial application`() {
    val sh = shell()
    // arr.contains returns a bound function — use it as a predicate
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(3.0))),
      sh.eval("let allowed = [2, 3, 5]; [1, 2, 3, 4] |> filter(allowed.contains)")
    )
  }

  @Test fun `array push mutates and returns new array`() {
    val sh = shell()
    assertEquals(TShellValue.TArray(listOf(TShellValue.TNumber(1.0), TShellValue.TNumber(2.0), TShellValue.TNumber(3.0))),
      sh.eval("let arr = [1, 2]; arr.push(3); arr"))
  }

  @Test fun `switch works`() {
    val sh = shell()
    assertEquals(TShellValue.TString("one"), sh.eval("""
      let x = 1
      switch (x) {
        case 1: "one"; break;
        case 2: "two"; break;
        default: "other"
      }
    """.trimIndent()))
  }

  @Test fun `async suggests all`() {
    val msg = errorMessage("async function f() {}")
    assertTrue(msg.contains("all()"), msg)
  }

  @Test fun `fn suggests function or arrow`() {
    val msg = errorMessage("fn f() {}")
    assertTrue(msg.contains("function") || msg.contains("=>"), msg)
  }

  @Test fun `import explains no imports`() {
    val msg = errorMessage("import x from 'y'")
    assertTrue(msg.contains("import"), msg)
  }

  @Test fun `try-catch works`() {
    val sh = shell()
    assertEquals(TShellValue.TString("caught: boom"), sh.eval("""
      try { throw "boom" } catch(e) { "caught: " + e }
    """))
  }

  @Test fun `try-catch with fail`() {
    val sh = shell()
    assertEquals(TShellValue.TString("ok"), sh.eval("""
      try { fail("nope") } catch(e) { "ok" }
    """))
  }

  @Test fun `try-finally runs finally`() {
    val sh = shell()
    assertEquals(TShellValue.TNumber(1.0), sh.eval("""
      let x = 0
      try { x = 1 } finally { x = x }
      x
    """))
  }

  @Test fun `delete explains immutability`() {
    val msg = errorMessage("let o = {a: 1}; delete o.a")
    assertTrue(msg.contains("immutable"), msg)
  }

  @Test fun `Promise all resolves to all`() {
    val sh = shell()
    assertEquals(TShellValue.TArray(listOf(TShellValue.TNumber(1.0), TShellValue.TNumber(2.0))),
      sh.eval("Promise.all(() => 1, () => 2)"))
  }

  @Test fun `typeof operator works`() {
    val sh = shell()
    assertEquals(TString("number"), sh.eval("typeof 42"))
    assertEquals(TString("string"), sh.eval("""typeof "hello""""))
    assertEquals(TString("array"), sh.eval("typeof [1, 2]"))
    assertEquals(TString("null"), sh.eval("typeof null"))
    assertEquals(TString("boolean"), sh.eval("typeof true"))
    assertEquals(TString("object"), sh.eval("typeof {a: 1}"))
  }

  @Test fun `typeof in condition`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("""typeof 42 == "number""""))
  }

  // --- for-in ---

  @Test fun `for-in iterates object keys`() {
    val sh = shell()
    assertEquals(TShellValue.TArray(listOf(TString("a"), TString("b"))),
      sh.eval("let result = []; for (let k in {a: 1, b: 2}) { result.push(k) }; result"))
  }

  @Test fun `for-in iterates array indices`() {
    val sh = shell()
    assertEquals(TShellValue.TArray(listOf(TShellValue.TNumber(0.0), TShellValue.TNumber(1.0), TShellValue.TNumber(2.0))),
      sh.eval("let result = []; for (let i in [10, 20, 30]) { result.push(i) }; result"))
  }

  // --- in operator ---

  @Test fun `in operator checks object keys`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval(""""a" in {a: 1, b: 2}"""))
    assertEquals(TBoolean(false), sh.eval(""""c" in {a: 1, b: 2}"""))
  }

  @Test fun `in operator checks array membership`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("2 in [1, 2, 3]"))
    assertEquals(TBoolean(false), sh.eval("5 in [1, 2, 3]"))
  }

  // --- at() with negative indexing ---

  @Test fun `at with negative index on array`() {
    val sh = shell()
    assertEquals(TShellValue.TNumber(3.0), sh.eval("[1, 2, 3].at(-1)"))
    assertEquals(TShellValue.TNumber(1.0), sh.eval("[1, 2, 3].at(0)"))
  }

  @Test fun `at with negative index on string`() {
    val sh = shell()
    assertEquals(TString("o"), sh.eval(""""hello".at(-1)"""))
  }
}
