package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
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

  @Test fun `var suggests let`() {
    val msg = errorMessage("var x = 5")
    assertTrue(msg.contains("let"), msg)
  }

  @Test fun `console dot log suggests print`() {
    val msg = errorMessage("""console.log("hi")""")
    assertTrue(msg.contains("print"), msg)
  }

  @Test fun `JSON dot parse suggests parseJson`() {
    val msg = errorMessage("""JSON.parse("{}")""")
    assertTrue(msg.contains("parseJson"), msg)
  }

  @Test fun `Math dot floor suggests top-level`() {
    val msg = errorMessage("Math.floor(3.5)")
    assertTrue(msg.contains("floor(3.7)"), msg)
  }

  @Test fun `Object dot keys suggests keys`() {
    val msg = errorMessage("Object.keys({a: 1})")
    assertTrue(msg.contains("keys()"), msg)
  }

  @Test fun `new suggests no classes`() {
    val msg = errorMessage("new Date()")
    assertTrue(msg.contains("no constructors"), msg)
  }

  @Test fun `class suggests objects and functions`() {
    val msg = errorMessage("class Foo {}")
    assertTrue(msg.contains("objects and functions"), msg)
  }

  @Test fun `throw suggests fail`() {
    val msg = errorMessage("""throw "error"""")
    assertTrue(msg.contains("fail"), msg)
  }

  @Test fun `String constructor suggests str`() {
    val msg = errorMessage("String(42)")
    assertTrue(msg.contains("str()"), msg)
  }

  @Test fun `Number constructor suggests num`() {
    val msg = errorMessage("""Number("42")""")
    assertTrue(msg.contains("num()"), msg)
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

  @Test fun `array push explains immutability`() {
    val msg = errorMessage("[1,2].push(3)")
    assertTrue(msg.contains("immutable"), msg)
  }

  @Test fun `switch suggests if-else`() {
    val msg = errorMessage("let x = 1; switch(x) {}")
    assertTrue(msg.contains("if/else"), msg)
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

  @Test fun `try-catch explains no support`() {
    val msg = errorMessage("try {} catch(e) {}")
    assertTrue(msg.contains("try/catch"), msg)
  }

  @Test fun `delete explains immutability`() {
    val msg = errorMessage("let o = {a: 1}; delete o.a")
    assertTrue(msg.contains("immutable"), msg)
  }

  @Test fun `Promise suggests all`() {
    val msg = errorMessage("Promise.all([])")
    assertTrue(msg.contains("all()"), msg)
  }
}
