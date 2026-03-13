package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class TShellTest {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    sh.register("echo", "value: any", "returns the value", listOf("echo(42)")) { args ->
      args.firstOrNull() ?: TNull
    }
    sh.register("add", "a: number, b: number", "adds two numbers") { args ->
      val a = (args[0] as TNumber).value
      val b = (args[1] as TNumber).value
      TNumber(a + b)
    }
    sh.register("double", "x: number", "doubles a number") { args ->
      val x = (args[0] as TNumber).value
      TNumber(x * 2)
    }
    sh.register("toUpper", "s: string", "uppercases a string") { args ->
      val s = (args[0] as TString).value
      TString(s.uppercase())
    }
    sh.register("items", "", "returns a test array") {
      TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0)))
    }
    return sh
  }

  // --- Arithmetic ---

  @Test fun `arithmetic operations`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("3 + 4"))
    assertEquals(TNumber(10.0), sh.eval("20 - 10"))
    assertEquals(TNumber(12.0), sh.eval("3 * 4"))
    assertEquals(TNumber(5.0), sh.eval("10 / 2"))
    assertEquals(TNumber(1.0), sh.eval("7 % 3"))
  }

  @Test fun `operator precedence`() {
    val sh = shell()
    assertEquals(TNumber(14.0), sh.eval("2 + 3 * 4"))
    assertEquals(TNumber(20.0), sh.eval("(2 + 3) * 4"))
  }

  @Test fun `unary operators`() {
    val sh = shell()
    assertEquals(TNumber(-5.0), sh.eval("-5"))
    assertEquals(TBoolean(false), sh.eval("!true"))
    assertEquals(TBoolean(true), sh.eval("!false"))
  }

  // --- Comparison & Logic ---

  @Test fun `comparison operators`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("3 < 5"))
    assertEquals(TBoolean(false), sh.eval("5 < 3"))
    assertEquals(TBoolean(true), sh.eval("5 >= 5"))
    assertEquals(TBoolean(true), sh.eval("3 == 3"))
    assertEquals(TBoolean(true), sh.eval("3 != 4"))
  }

  @Test fun `logical operators`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("true && true"))
    assertEquals(TBoolean(false), sh.eval("true && false"))
    assertEquals(TBoolean(true), sh.eval("false || true"))
  }

  @Test fun `ternary expression`() {
    val sh = shell()
    assertEquals(TString("yes"), sh.eval("true ? \"yes\" : \"no\""))
    assertEquals(TString("no"), sh.eval("false ? \"yes\" : \"no\""))
  }

  // --- Strings ---

  @Test fun `string concatenation`() {
    val sh = shell()
    assertEquals(TString("hello world"), sh.eval("\"hello\" + \" \" + \"world\""))
  }

  @Test fun `string coercion in addition`() {
    val sh = shell()
    assertEquals(TString("count: 5"), sh.eval("\"count: \" + 5"))
  }

  // --- Variables ---

  @Test fun `let declares variables`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("let x = 42; x"))
    assertEquals(TNumber(42.0), sh.eval("let y = 42; y"))
  }

  // --- Arrays & Objects ---

  @Test fun `array literal and access`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("[1, 2, 3][1]"))
    assertEquals(TNumber(3.0), sh.eval("[1, 2, 3].length"))
  }

  @Test fun `object literal and access`() {
    val sh = shell()
    assertEquals(TNumber(30.0), sh.eval("{name: \"alice\", age: 30}.age"))
  }

  @Test fun `spread array`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0), TNumber(4.0))),
      sh.eval("let a = [1, 2]; [...a, 3, 4]")
    )
  }

  @Test fun `spread object`() {
    val sh = shell()
    val result = sh.eval("let a = {x: 1}; {...a, y: 2}")
    assertTrue(result is TObject)
    assertEquals(TNumber(1.0), (result as TObject).fields["x"])
    assertEquals(TNumber(2.0), result.fields["y"])
  }

  @Test fun `destructuring object`() {
    val sh = shell()
    assertEquals(TNumber(30.0), sh.eval("let {name, age} = {name: \"alice\", age: 30}; age"))
  }

  @Test fun `destructuring array`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("let [a, b] = [1, 2]; b"))
  }

  // --- Functions ---

  @Test fun `function declaration and call`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("function square(x) { return x * x }; square(3) - 3"))
  }

  @Test fun `arrow function`() {
    val sh = shell()
    assertEquals(TNumber(9.0), sh.eval("let sq = x => x * x; sq(3)"))
  }

  @Test fun `multi-param arrow`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("let add = (a, b) => a + b; add(3, 4)"))
  }

  // --- Control flow ---

  @Test fun `if else`() {
    val sh = shell()
    assertEquals(TString("big"), sh.eval("let x = 10; if (x > 5) { \"big\" } else { \"small\" }"))
  }

  @Test fun `while loop`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("""
      let x = 0
      let i = 0
      while (i < 5) {
        x = x + i
        i = i + 1
      }
      x
    """))
  }

  @Test fun `for of loop`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("""
      let sum = 0
      for (let x of [1, 2, 3]) {
        sum = sum + x
      }
      sum
    """))
  }

  // --- Commands ---

  @Test fun `registered command call`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("echo(42)"))
    assertEquals(TNumber(7.0), sh.eval("add(3, 4)"))
  }

  @Test fun `unknown command gives helpful error`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("eccho(42)") }
    assertTrue(err.message!!.contains("echo"), "Error should suggest 'echo': ${err.message}")
  }

  // --- Pipes ---

  @Test fun `basic pipe`() {
    val sh = shell()
    assertEquals(TNumber(84.0), sh.eval("echo(42) |> double"))
  }

  @Test fun `multi-step pipe`() {
    val sh = shell()
    assertEquals(TNumber(168.0), sh.eval("echo(42) |> double |> double"))
  }

  @Test fun `pipe with arrow function`() {
    val sh = shell()
    assertEquals(TNumber(43.0), sh.eval("echo(42) |> (x => x + 1)"))
  }

  @Test fun `pipe with curried commands`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(4.0), TNumber(6.0))),
      sh.eval("items() |> map(x => x * 2)")
    )
  }

  @Test fun `pipe with filter`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(3.0))),
      sh.eval("items() |> filter(x => x > 1)")
    )
  }

  // --- Composition ---

  @Test fun `chain sequential`() {
    val sh = shell()
    assertEquals(TNumber(84.0), sh.eval("chain(() => 42, x => x * 2)"))
  }

  @Test fun `all parallel`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))),
      sh.eval("all(() => 1, () => 2, () => 3)")
    )
  }

  @Test fun `race first success`() {
    val sh = shell()
    // True race: either 42 or 99 can win (fail("nope") always loses)
    val result = sh.eval("race(() => fail(\"nope\"), () => 42, () => 99)")
    assertTrue(result == TNumber(42.0) || result == TNumber(99.0),
      "Expected 42 or 99, got $result")
  }

  @Test fun `race all fail`() {
    val sh = shell()
    assertThrows<TShellError> { sh.eval("race(() => fail(\"a\"), () => fail(\"b\"))") }
  }

  // --- Containment ---

  @Test fun `no ambient access`() {
    val sh = TShell() // no commands registered
    assertThrows<TShellError> { sh.eval("process") }
    assertThrows<TShellError> { sh.eval("require") }
  }

  // --- Error quality ---

  @Test fun `syntax error shows location`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("let x = ") }
    assertTrue(err.message!!.contains("Syntax error"), "Should have syntax error: ${err.message}")
  }

  @Test fun `type mismatch in pipe shows hint`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("42 |> 43") }
    assertTrue(err.message!!.contains("function"), "Should mention function: ${err.message}")
  }

  @Test fun `pipe left adds right-side args`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("3 |> add <| 4"))
  }

  @Test fun `pipe left multiple args`() {
    val sh = shell()
    // add takes 2 args, but test with a 3-arg function
    sh.register("sum3", "a, b, c", "sum of three", emptyList()) { args ->
      val a = (args[0] as TShellValue.TNumber).value
      val b = (args[1] as TShellValue.TNumber).value
      val c = (args[2] as TShellValue.TNumber).value
      TShellValue.TNumber(a + b + c)
    }
    assertEquals(TNumber(6.0), sh.eval("1 |> sum3 <| 2 <| 3"))
  }

  @Test fun `pipe chain with left pipe`() {
    val sh = shell()
    // 3 |> add <| 4 → 7, then |> double → 14
    assertEquals(TNumber(14.0), sh.eval("3 |> add <| 4 |> double"))
  }

  @Test fun `pipe destructure into array`() {
    val sh = shell()
    sh.eval("[10, 20, 30] |> [a, b, c]")
    assertEquals(TNumber(10.0), sh.eval("a"))
    assertEquals(TNumber(20.0), sh.eval("b"))
    assertEquals(TNumber(30.0), sh.eval("c"))
  }

  @Test fun `pipe destructure passthrough for chaining`() {
    val sh = shell()
    val result = sh.eval("[1, 2, 3] |> [x, y, z] |> (arr => arr)")
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))), result)
  }

  @Test fun `pipe destructure with fewer names than elements`() {
    val sh = shell()
    sh.eval("[10, 20, 30] |> [a, b]")
    assertEquals(TNumber(10.0), sh.eval("a"))
    assertEquals(TNumber(20.0), sh.eval("b"))
  }

  @Test fun `pipe destructure with more names than elements`() {
    val sh = shell()
    sh.eval("[10] |> [a, b, c]")
    assertEquals(TNumber(10.0), sh.eval("a"))
    assertEquals(TShellValue.TNull, sh.eval("b"))
    assertEquals(TShellValue.TNull, sh.eval("c"))
  }

  // --- Scatter pipe |* ---

  @Test fun `scatter pipe maps function over array in parallel`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(4.0), TNumber(6.0))),
      sh.eval("[1, 2, 3] |* double")
    )
  }

  @Test fun `scatter pipe normalizes null to empty array`() {
    val sh = shell()
    assertEquals(TArray(emptyList()), sh.eval("null |* double"))
  }

  @Test fun `scatter pipe normalizes non-array to single-element array`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(10.0))), sh.eval("5 |* double"))
  }

  @Test fun `scatter pipe with arrow function`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(4.0), TNumber(9.0))),
      sh.eval("[1, 2, 3] |* (x => x * x)")
    )
  }

  @Test fun `scatter pipe with extra args via pipe left`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(11.0), TNumber(12.0), TNumber(13.0))),
      sh.eval("[1, 2, 3] |* add <| 10")
    )
  }

  @Test fun `scatter then reduce`() {
    val sh = shell()
    assertEquals(
      TNumber(12.0),
      sh.eval("[1, 2, 3] |* double |> reduce((acc, x) => acc + x)")
    )
  }

  // --- toArray ---

  @Test fun `toArray normalizes null to empty array`() {
    val sh = shell()
    assertEquals(TArray(emptyList()), sh.eval("null |> toArray()"))
  }

  @Test fun `toArray wraps non-array in array`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(5.0))), sh.eval("5 |> toArray()"))
  }

  @Test fun `toArray passes array through`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))),
      sh.eval("[1, 2, 3] |> toArray()")
    )
  }

  // --- Named arguments ---

  @Test fun `named args basic`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("""
      let sub = (a, b) => a - b
      sub(b: 3, a: 10)
    """.trimIndent()))
  }

  @Test fun `named args mixed with positional`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("""
      let sub = (a, b) => a - b
      sub(10, b: 3)
    """.trimIndent()))
  }

  @Test fun `named args reorder`() {
    val sh = shell()
    sh.register("concat3", "a, b, c", "concat three strings") { args ->
      val a = (args[0] as TShellValue.TString).value
      val b = (args[1] as TShellValue.TString).value
      val c = (args[2] as TShellValue.TString).value
      TShellValue.TString("$a$b$c")
    }
    assertEquals(TShellValue.TString("xyz"), sh.eval("""
      function f(a, b, c) { return concat3(a, b, c) }
      f(c: "z", a: "x", b: "y")
    """.trimIndent()))
  }

  @Test fun `named args unknown name errors`() {
    val sh = shell()
    val err = assertThrows<TShellError> {
      sh.eval("""
        let f = (a, b) => a + b
        f(x: 1, y: 2)
      """.trimIndent())
    }
    assertTrue(err.message!!.contains("Unknown named argument"))
  }

  @Test fun `named args conflict with positional errors`() {
    val sh = shell()
    val err = assertThrows<TShellError> {
      sh.eval("""
        let f = (a, b) => a + b
        f(1, a: 2)
      """.trimIndent())
    }
    assertTrue(err.message!!.contains("conflicts"))
  }

  @Test fun `named args duplicate errors`() {
    val sh = shell()
    val err = assertThrows<TShellError> {
      sh.eval("""
        let f = (a, b) => a + b
        f(a: 1, a: 2)
      """.trimIndent())
    }
    assertTrue(err.message!!.contains("Duplicate"))
  }

  // --- Prompt generation ---

  @Test fun `toPrompt lists commands`() {
    val sh = shell()
    val prompt = sh.toPrompt()
    assertTrue(prompt.contains("echo"))
    assertTrue(prompt.contains("add"))
    assertTrue(prompt.contains("help"))
  }

  // --- let keyword ---

  @Test fun `let declares a variable`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("let x = 42; x"))
  }

  @Test fun `let is reassignable`() {
    val sh = shell()
    assertEquals(TNumber(99.0), sh.eval("let x = 42; x = 99; x"))
  }

  @Test fun `let supports destructuring`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("let {a, b} = {a: 1, b: 2}; a + b"))
  }

  @Test fun `let works in for-of loops`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("""
      let sum = 0
      for (let x of [1, 2, 3]) { sum += x }
      sum
    """))
  }

  @Test fun `let has block scope`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("""
      let x = 1
      if (true) { let x = 99 }
      x
    """))
  }

  @Test fun `set without definition throws error`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("x = 42") }
    assertTrue(err.message!!.contains("not defined"))
  }

}
