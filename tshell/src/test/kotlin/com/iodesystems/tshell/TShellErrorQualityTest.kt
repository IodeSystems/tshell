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
}
