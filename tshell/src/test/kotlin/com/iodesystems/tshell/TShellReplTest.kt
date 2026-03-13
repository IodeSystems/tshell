package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class TShellReplTest {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  // --- Incremental eval (REPL pattern) ---

  @Test fun `globals persist across eval calls`() {
    val sh = shell()
    sh.eval("let x = 42")
    assertEquals(TNumber(42.0), sh.eval("x"))
  }

  @Test fun `functions persist across eval calls`() {
    val sh = shell()
    sh.eval("function double(x) { return x * 2 }")
    assertEquals(TNumber(84.0), sh.eval("double(42)"))
  }

  @Test fun `incremental definition building`() {
    val sh = shell()
    sh.eval("function add(a, b) { return a + b }")
    sh.eval("function double(x) { return add(x, x) }")
    sh.eval("let data = [1, 2, 3]")
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(4.0), TNumber(6.0))),
      sh.eval("data |> map(double)")
    )
  }

  // --- Interpreted function + native toolkit interaction ---

  @Test fun `function declarations work with map`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(4.0), TNumber(6.0))),
      sh.eval("function double(x) { return x * 2 }; [1, 2, 3] |> map(double)")
    )
  }

  @Test fun `function declarations work with filter`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(4.0))),
      sh.eval("function isEven(x) { return x % 2 == 0 }; [1, 2, 3, 4] |> filter(isEven)")
    )
  }

  @Test fun `function declarations work with reduce`() {
    val sh = shell()
    assertEquals(
      TNumber(10.0),
      sh.eval("function add(a, b) { return a + b }; [1, 2, 3, 4] |> reduce(add, 0)")
    )
  }

  @Test fun `function declarations work with find`() {
    val sh = shell()
    assertEquals(
      TNumber(3.0),
      sh.eval("function isThree(x) { return x == 3 }; [1, 2, 3, 4] |> find(isThree)")
    )
  }

  @Test fun `function declarations work in chain`() {
    val sh = shell()
    assertEquals(
      TNumber(6.0),
      sh.eval("""
        function getData() { return [1, 2, 3] }
        function sum(arr) { return arr |> reduce((a, b) => a + b, 0) }
        chain(getData, sum)
      """)
    )
  }

  // --- Pipe precedence ---

  @Test fun `pipe binds tighter than comparison`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("[1, 2, 3] |> len() > 2"))
    assertEquals(TBoolean(false), sh.eval("[1, 2, 3] |> len() > 5"))
  }

  @Test fun `pipe binds tighter than equality`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("[1, 2, 3] |> len() == 3"))
  }

  @Test fun `pipe binds tighter than logical`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("[1, 2, 3] |> len() > 2 && true"))
  }

  @Test fun `pipe in ternary condition`() {
    val sh = shell()
    assertEquals(TString("many"), sh.eval("[1, 2, 3] |> len() > 2 ? \"many\" : \"few\""))
  }

  // --- evalTransactional: rollback on failure ---

  @Test fun `evalTransactional rolls back on failure`() {
    val sh = shell()
    sh.eval("let x = 1")
    val result = sh.evalTransactional("x = 42; fail(\"boom\")")
    assertTrue(result.isFailure)
    assertEquals(TNumber(1.0), sh.eval("x"))
  }

  @Test fun `evalTransactional preserves state on success`() {
    val sh = shell()
    sh.eval("let x = 1")
    val result = sh.evalTransactional("x = 42; x")
    assertTrue(result.isSuccess)
    assertEquals(TNumber(42.0), result.getOrNull())
    assertEquals(TNumber(42.0), sh.eval("x"))
  }

  // --- State extraction and injection ---

  @Test fun `getState returns all globals`() {
    val sh = shell()
    sh.eval("let name = \"alice\"")
    sh.eval("let age = 30")
    val state = sh.getState()
    assertEquals(TString("alice"), state.fields["name"])
    assertEquals(TNumber(30.0), state.fields["age"])
  }

  @Test fun `setState injects values`() {
    val sh = shell()
    sh.setState(mapOf(
      "x" to TNumber(42.0),
      "name" to TString("bob")
    ))
    assertEquals(TNumber(42.0), sh.eval("x"))
    assertEquals(TString("bob"), sh.eval("name"))
  }

  @Test fun `state transfer between shells`() {
    val sh1 = shell()
    sh1.eval("let x = 42")
    sh1.eval("let name = \"alice\"")
    val state = sh1.getState()

    val sh2 = shell()
    sh2.setState(state)
    assertEquals(TNumber(42.0), sh2.eval("x"))
    assertEquals(TString("alice"), sh2.eval("name"))
  }

  @Test fun `state includes functions`() {
    val sh = shell()
    sh.eval("function double(x) { return x * 2 }")
    val state = sh.getState()
    assertTrue(state.fields["double"] is TFunction)
  }

  @Test fun `injected state can be overwritten`() {
    val sh = shell()
    sh.setState(mapOf("x" to TNumber(1.0)))
    sh.eval("x = 42")
    assertEquals(TNumber(42.0), sh.eval("x"))
  }

  // --- evalExported: only exported names persist ---

  @Test fun `evalExported discards non-exported state`() {
    val sh = shell()
    sh.evalExported("let x = 42")
    assertThrows<TShellError> { sh.eval("x") }
  }

  @Test fun `evalExported keeps exported val`() {
    val sh = shell()
    sh.evalExported("export let x = 42")
    assertEquals(TNumber(42.0), sh.eval("x"))
  }

  @Test fun `evalExported keeps exported fn`() {
    val sh = shell()
    sh.evalExported("export function double(n) { return n * 2 }")
    assertEquals(TNumber(84.0), sh.eval("double(42)"))
  }

  @Test fun `evalExported re-export redefines`() {
    val sh = shell()
    sh.evalExported("export let x = 1")
    sh.evalExported("export x = 99")
    assertEquals(TNumber(99.0), sh.eval("x"))
  }

  @Test fun `evalExported discards all on error`() {
    val sh = shell()
    sh.evalExported("export let x = 42")
    assertThrows<TShellError> { sh.evalExported("export let y = 1; fail(\"boom\")") }
    assertEquals(TNumber(42.0), sh.eval("x"))
    assertThrows<TShellError> { sh.eval("y") }
  }

  @Test fun `evalExported can use globals from prior exports`() {
    val sh = shell()
    sh.evalExported("export let x = 10")
    assertEquals(TNumber(20.0), sh.evalExported("x * 2"))
  }

  @Test fun `evalExported destructure export`() {
    val sh = shell()
    sh.evalExported("""export let {name, age} = {name: "alice", age: 30}""")
    assertEquals(TString("alice"), sh.eval("name"))
    assertEquals(TNumber(30.0), sh.eval("age"))
  }
}
