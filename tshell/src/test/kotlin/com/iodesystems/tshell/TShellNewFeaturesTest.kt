package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class TShellNewFeaturesTest {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  // --- Nullish coalescing ---

  @Test fun `nullish coalescing returns left when non-null`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("42 ?? 99"))
  }

  @Test fun `nullish coalescing returns right when null`() {
    val sh = shell()
    assertEquals(TNumber(99.0), sh.eval("null ?? 99"))
  }

  @Test fun `nullish coalescing chains`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("null ?? null ?? 3"))
  }

  @Test fun `nullish coalescing keeps falsy non-null`() {
    val sh = shell()
    assertEquals(TNumber(0.0), sh.eval("0 ?? 99"))
    assertEquals(TString(""), sh.eval("\"\" ?? \"default\""))
    assertEquals(TBoolean(false), sh.eval("false ?? true"))
  }

  // --- Optional chaining ---

  @Test fun `optional chaining on object`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("let x = {a: {b: 1}}; x?.a?.b"))
  }

  @Test fun `optional chaining on null`() {
    val sh = shell()
    assertEquals(TNull, sh.eval("let x = null; x?.a?.b"))
  }

  @Test fun `optional chaining with nullish coalescing`() {
    val sh = shell()
    assertEquals(TString("default"), sh.eval("let x = null; x?.name ?? \"default\""))
  }

  @Test fun `optional chaining on nested null`() {
    val sh = shell()
    assertEquals(TNull, sh.eval("let x = {a: null}; x.a?.b"))
  }

  // --- Compound assignment ---

  @Test fun `plus equals`() {
    val sh = shell()
    sh.eval("let x = 10")
    sh.eval("x += 5")
    assertEquals(TNumber(15.0), sh.eval("x"))
  }

  @Test fun `minus equals`() {
    val sh = shell()
    sh.eval("let x = 10")
    sh.eval("x -= 3")
    assertEquals(TNumber(7.0), sh.eval("x"))
  }

  @Test fun `star equals`() {
    val sh = shell()
    sh.eval("let x = 4")
    sh.eval("x *= 3")
    assertEquals(TNumber(12.0), sh.eval("x"))
  }

  @Test fun `plus equals string concat`() {
    val sh = shell()
    sh.eval("let s = \"hello\"")
    sh.eval("s += \" world\"")
    assertEquals(TString("hello world"), sh.eval("s"))
  }

  // --- Member assignment ---

  @Test fun `assign to object field`() {
    val sh = shell()
    sh.eval("let obj = {x: 1, y: 2}")
    sh.eval("obj.x = 42")
    assertEquals(TNumber(42.0), sh.eval("obj.x"))
    assertEquals(TNumber(2.0), sh.eval("obj.y"))
  }

  @Test fun `assign to nested object field`() {
    val sh = shell()
    sh.eval("let obj = {a: {b: {c: 1}}}")
    sh.eval("obj.a.b.c = 99")
    assertEquals(TNumber(99.0), sh.eval("obj.a.b.c"))
  }

  @Test fun `assign to array index`() {
    val sh = shell()
    sh.eval("let arr = [1, 2, 3]")
    sh.eval("arr[1] = 42")
    assertEquals(TNumber(42.0), sh.eval("arr[1]"))
  }

  @Test fun `compound assign to object field`() {
    val sh = shell()
    sh.eval("let obj = {count: 10}")
    sh.eval("obj.count += 5")
    assertEquals(TNumber(15.0), sh.eval("obj.count"))
  }

  @Test fun `assign new field to object`() {
    val sh = shell()
    sh.eval("let obj = {x: 1}")
    sh.eval("obj.y = 2")
    assertEquals(TNumber(2.0), sh.eval("obj.y"))
  }

  // --- Break / Continue ---

  @Test fun `break exits loop`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("""
      let sum = 0
      for (let i of [1, 2, 3, 4, 5]) {
        if (i > 3) { break }
        sum += i
      }
      sum
    """))
  }

  @Test fun `continue skips iteration`() {
    val sh = shell()
    assertEquals(TNumber(12.0), sh.eval("""
      let sum = 0
      for (let i of [1, 2, 3, 4, 5]) {
        if (i == 3) { continue }
        sum += i
      }
      sum
    """))
  }

  @Test fun `break in while loop`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("""
      let i = 0
      while (true) {
        i += 1
        if (i == 5) { break }
      }
      i
    """))
  }

  // --- Execution limits ---

  @Test fun `step limit prevents infinite loop`() {
    val sh = TShell(maxSteps = 100)
    CoreToolkit.install(sh)
    val err = assertThrows<TShellError> { sh.eval("let i = 0; while (true) { i += 1 }") }
    assertTrue(err.message!!.contains("step limit"))
    assertTrue(err.message!!.contains("extendLimit"))
  }

  @Test fun `call depth limit prevents infinite recursion`() {
    val sh = TShell(maxCallDepth = 10)
    CoreToolkit.install(sh)
    val err = assertThrows<TShellError> { sh.eval("function f(n) { f(n + 1) }; f(0)") }
    assertTrue(err.message!!.contains("Call stack depth"))
    assertTrue(err.message!!.contains("extendLimit"))
  }

  @Test fun `timeout prevents long-running programs`() {
    val sh = TShell(timeoutMs = 50, maxSteps = 100_000_000)
    CoreToolkit.install(sh)
    val err = assertThrows<TShellError> { sh.eval("let i = 0; while (true) { i += 1 }") }
    assertTrue(err.message!!.contains("timeout"))
    assertTrue(err.message!!.contains("extendLimit"))
  }

  @Test fun `extendLimit increases limit within same eval`() {
    val sh = TShell(maxSteps = 50)
    CoreToolkit.install(sh)
    // First: confirm it would fail
    assertThrows<TShellError> {
      sh.eval("for (let i of range(0, 100)) { i }")
    }
    // Extend and do work in same eval
    sh.eval("extendLimit({steps: 200}); for (let i of range(0, 100)) { i }")
  }

  @Test fun `limits reset between evals by default`() {
    val sh = TShell(maxSteps = 50)
    CoreToolkit.install(sh)
    // Extend in one eval
    sh.eval("extendLimit({steps: 200})")
    // Next eval resets — should fail again
    assertThrows<TShellError> {
      sh.eval("for (let i of range(0, 100)) { i }")
    }
  }

  @Test fun `limits persist when resetOnEval is false`() {
    val sh = TShell(maxSteps = 50, resetLimitsOnEval = false)
    CoreToolkit.install(sh)
    // Extend in one eval
    sh.eval("extendLimit({steps: 200})")
    // Next eval keeps extended limits — should succeed
    sh.eval("for (let i of range(0, 100)) { i }")
  }

  @Test fun `extendLimit rejects lower value`() {
    val sh = shell()
    val err = assertThrows<TShellError> { sh.eval("extendLimit({steps: 100})") }
    assertTrue(err.message!!.contains("must be greater"))
  }

  @Test fun `limits shows current limits`() {
    val sh = TShell(maxSteps = 500, maxCallDepth = 64, timeoutMs = 5000)
    CoreToolkit.install(sh)
    val result = sh.eval("limits()") as TObject
    assertEquals(TNumber(500.0), result.fields["maxSteps"])
    assertEquals(TNumber(64.0), result.fields["maxCallDepth"])
    assertEquals(TNumber(5000.0), result.fields["timeoutMs"])
  }

  @Test fun `step counter fires on pipe chains`() {
    val sh = TShell(maxSteps = 5)
    CoreToolkit.install(sh)
    val err = assertThrows<TShellError> {
      sh.eval("[1,2,3,4,5,6,7,8,9,10] |> map(x => x) |> map(x => x) |> map(x => x) |> map(x => x) |> map(x => x)")
    }
    assertTrue(err.message!!.contains("step limit"))
  }

  // --- String operations ---

  @Test fun `trim removes whitespace`() {
    val sh = shell()
    assertEquals(TString("hello"), sh.eval("\"  hello  \" |> trim()"))
  }

  @Test fun `lower and upper`() {
    val sh = shell()
    assertEquals(TString("hello"), sh.eval("\"Hello\" |> lower()"))
    assertEquals(TString("HELLO"), sh.eval("\"Hello\" |> upper()"))
  }

  @Test fun `replace in string`() {
    val sh = shell()
    assertEquals(TString("hello tshell"), sh.eval("\"hello world\" |> replace(\"world\", \"tshell\")"))
  }

  @Test fun `startsWith and endsWith`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("\"hello\" |> startsWith(\"hel\")"))
    assertEquals(TBoolean(false), sh.eval("\"hello\" |> startsWith(\"world\")"))
    assertEquals(TBoolean(true), sh.eval("\"hello\" |> endsWith(\"llo\")"))
  }

  @Test fun `indexOf returns position`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("\"hello\" |> indexOf(\"ll\")"))
    assertEquals(TNumber(-1.0), sh.eval("\"hello\" |> indexOf(\"xyz\")"))
  }

  @Test fun `substring extracts range`() {
    val sh = shell()
    assertEquals(TString("ell"), sh.eval("\"hello\" |> substring(1, 4)"))
    assertEquals(TString("llo"), sh.eval("\"hello\" |> substring(2)"))
  }

  @Test fun `match returns regex matches`() {
    val sh = shell()
    val result = sh.eval("\"abc123def456\" |> match(\"[0-9]+\")") as TArray
    assertEquals(2, result.elements.size)
    assertEquals(TString("123"), result.elements[0])
    assertEquals(TString("456"), result.elements[1])
  }

  // --- Math operations ---

  @Test fun `floor rounds down`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("3.7 |> floor()"))
    assertEquals(TNumber(-4.0), sh.eval("-3.2 |> floor()"))
  }

  @Test fun `ceil rounds up`() {
    val sh = shell()
    assertEquals(TNumber(4.0), sh.eval("3.2 |> ceil()"))
  }

  @Test fun `round rounds nearest`() {
    val sh = shell()
    assertEquals(TNumber(4.0), sh.eval("3.5 |> round()"))
    assertEquals(TNumber(3.0), sh.eval("3.4 |> round()"))
  }

  @Test fun `abs returns absolute value`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("-5 |> abs()"))
    assertEquals(TNumber(5.0), sh.eval("5 |> abs()"))
  }

  @Test fun `min and max with args`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("min(3, 1, 2)"))
    assertEquals(TNumber(3.0), sh.eval("max(3, 1, 2)"))
  }

  @Test fun `min and max piped from array`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("[3, 1, 2] |> min()"))
    assertEquals(TNumber(3.0), sh.eval("[3, 1, 2] |> max()"))
  }

  @Test fun `pow computes power`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("pow(2, 3)"))
  }

  // --- typeof ---

  @Test fun `typeof returns type names`() {
    val sh = shell()
    assertEquals(TString("number"), sh.eval("typeof(42)"))
    assertEquals(TString("string"), sh.eval("typeof(\"hello\")"))
    assertEquals(TString("boolean"), sh.eval("typeof(true)"))
    assertEquals(TString("null"), sh.eval("typeof(null)"))
    assertEquals(TString("array"), sh.eval("typeof([1, 2])"))
    assertEquals(TString("object"), sh.eval("typeof({a: 1})"))
  }

  // --- JSON ---

  @Test fun `parseJson parses object`() {
    val sh = shell()
    val result = sh.eval("""parseJson("{\"a\": 1, \"b\": \"hello\"}")""") as TObject
    assertEquals(TNumber(1.0), result.fields["a"])
    assertEquals(TString("hello"), result.fields["b"])
  }

  @Test fun `parseJson parses array`() {
    val sh = shell()
    val result = sh.eval("""parseJson("[1, 2, 3]")""") as TArray
    assertEquals(3, result.elements.size)
  }

  @Test fun `parseJson works as pipe`() {
    val sh = shell()
    val result = sh.eval(""""{\"x\": 42}" |> parseJson()""") as TObject
    assertEquals(TNumber(42.0), result.fields["x"])
  }

  @Test fun `parseJson handles nested structures`() {
    val sh = shell()
    val result = sh.eval("""parseJson("{\"a\": {\"b\": [1, true, null]}}")""") as TObject
    val inner = result.fields["a"] as TObject
    val arr = inner.fields["b"] as TArray
    assertEquals(TNumber(1.0), arr.elements[0])
    assertEquals(TBoolean(true), arr.elements[1])
    assertEquals(TNull, arr.elements[2])
  }

  @Test fun `toJson converts to string`() {
    val sh = shell()
    val result = sh.eval("""toJson({a: 1, b: "hello"})""") as TString
    assertTrue(result.value.contains("\"a\":1"))
    assertTrue(result.value.contains("\"b\":\"hello\""))
  }

  @Test fun `toJson works as pipe`() {
    val sh = shell()
    val result = sh.eval("""{x: [1, 2, 3]} |> toJson()""") as TString
    assertTrue(result.value.contains("\"x\":[1,2,3]"))
  }

  @Test fun `parseJson and toJson round-trip`() {
    val sh = shell()
    val original = """{"name":"alice","age":30,"active":true}"""
    val result = sh.eval("""parseJson("${original.replace("\"", "\\\"")}") |> toJson()""") as TString
    // Re-parse to verify structural equality
    val reparsed = sh.eval("""parseJson("${result.value.replace("\"", "\\\"")}")""") as TObject
    assertEquals(TString("alice"), reparsed.fields["name"])
    assertEquals(TNumber(30.0), reparsed.fields["age"])
    assertEquals(TBoolean(true), reparsed.fields["active"])
  }

  // --- Runtime line numbers ---

  @Test fun `runtime errors include line numbers`() {
    val sh = shell()
    val err = assertThrows<TShellError> {
      sh.eval("""
        let x = 1
        x |> map(n => n)
      """)
    }
    assertTrue(err.message!!.contains("at line"))
  }

  // --- Member access with keywords ---

  @Test fun `member access with keyword field names`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("{all: 1}.all"))
    assertEquals(TNumber(2.0), sh.eval("{for: 2}.for"))
    assertEquals(TNumber(3.0), sh.eval("{return: 3}.return"))
  }

  // --- Parallel all() ---

  @Test fun `all executes producers and returns array`() {
    val sh = shell()
    val result = sh.eval("all(() => 1, () => 2, () => 3)") as TArray
    assertEquals(3, result.elements.size)
    assertEquals(TNumber(1.0), result.elements[0])
    assertEquals(TNumber(2.0), result.elements[1])
    assertEquals(TNumber(3.0), result.elements[2])
  }

  @Test fun `all runs multiple producers`() {
    val sh = shell()
    val result = sh.eval("""
      all(
        () => range(0, 100) |> reduce((a, b) => a + b, 0),
        () => range(0, 100) |> reduce((a, b) => a + b, 0),
        () => range(0, 100) |> reduce((a, b) => a + b, 0)
      )
    """) as TArray
    assertEquals(3, result.elements.size)
    for (elem in result.elements) {
      assertEquals(TNumber(4950.0), elem)
    }
  }

  @Test fun `race returns first success`() {
    val sh = shell()
    val result = sh.eval("race(() => 42, () => 99)")
    // Should return one of the values (first to complete)
    assertTrue(result == TNumber(42.0) || result == TNumber(99.0))
  }

  @Test fun `race skips failures and returns first success`() {
    val sh = shell()
    // First producer throws, second succeeds
    val result = sh.eval("""race(() => error("fail"), () => 42)""")
    assertEquals(TNumber(42.0), result)
  }

  @Test fun `all with single producer works`() {
    val sh = shell()
    val result = sh.eval("all(() => 42)") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TNumber(42.0), result.elements[0])
  }

  @Test fun `race with all failures throws`() {
    val sh = shell()
    assertThrows<com.iodesystems.tshell.runtime.TShellError> {
      sh.eval("""race(() => error("a"), () => error("b"))""")
    }
  }

  // --- Strict equality (===, !==) ---

  @Test fun `triple equals works like double equals`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("1 === 1"))
    assertEquals(TBoolean(false), sh.eval("1 === 2"))
    assertEquals(TBoolean(true), sh.eval("\"hello\" === \"hello\""))
    assertEquals(TBoolean(false), sh.eval("\"hello\" === \"world\""))
  }

  @Test fun `strict not equals works like not equals`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("1 !== 2"))
    assertEquals(TBoolean(false), sh.eval("1 !== 1"))
  }

  // --- C-style for loops ---

  @Test fun `c-style for loop with let init`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("""
      let sum = 0
      for (let i = 0; i < 5; i += 1) {
        sum += i
      }
      sum
    """))
  }

  @Test fun `c-style for loop with existing variable`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("""
      let i = 0
      for (; i < 5; i += 1) {
      }
      i
    """))
  }

  @Test fun `c-style for loop with break`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("""
      let sum = 0
      for (let i = 0; i < 100; i += 1) {
        if (i === 3) break
        sum += 1
      }
      sum
    """))
  }

  @Test fun `c-style for loop with continue`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("""
      let sum = 0
      for (let i = 0; i < 5; i += 1) {
        if (i == 2) continue
        sum += i
      }
      sum
    """))
  }

  // --- Single-line if ---

  @Test fun `single-line if statement`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("""
      let x = 0
      if (true) x = 42
      x
    """))
  }

  @Test fun `single-line if with return`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("""
      function f(n) {
        if (n > 0) return n
        return 0
      }
      f(42)
    """))
  }

  // --- Increment / Decrement ---

  @Test fun `i++ increments variable`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("""
      let i = 0
      i++
      i
    """))
  }

  @Test fun `i-- decrements variable`() {
    val sh = shell()
    assertEquals(TNumber(-1.0), sh.eval("""
      let i = 0
      i--
      i
    """))
  }

  @Test fun `for loop with i++`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("""
      let sum = 0
      for (let i = 0; i < 5; i++) {
        sum += i
      }
      sum
    """))
  }

  @Test fun `for loop with i-- counts down`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("""
      let sum = 0
      for (let i = 4; i >= 0; i--) {
        sum += i
      }
      sum
    """))
  }

  @Test fun `increment on object field`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("""
      let obj = {count: 5}
      obj.count++
      obj.count
    """))
  }

  @Test fun `single-line if else`() {
    val sh = shell()
    assertEquals(TString("no"), sh.eval("""
      let x = 0
      if (false) x = 1
      else x = 2
      if (x == 2) "no"
      else "yes"
    """))
  }

  // --- Multi-binding let ---

  @Test fun `let multi-binding with initializers`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("let a = 1, b = 2; a + b"))
  }

  @Test fun `let multi-binding uninitialized defaults to null`() {
    val sh = shell()
    assertEquals(TNull, sh.eval("let a, b = 2; a"))
  }

  @Test fun `let multi-binding mixed initialized and uninitialized`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("let a, b, c = 2; c"))
  }

  @Test fun `let multi-binding different types`() {
    val sh = shell()
    assertEquals(TString("one"), sh.eval("""let a, b, c = 0, d = "one"; d"""))
  }

  @Test fun `let multi-binding all uninitialized`() {
    val sh = shell()
    assertEquals(TNull, sh.eval("let x, y, z; z"))
  }

  @Test fun `export let multi-binding`() {
    val sh = shell()
    sh.eval("export let a = 1, b = 2")
    assertEquals(TNumber(1.0), sh.eval("a"))
    assertEquals(TNumber(2.0), sh.eval("b"))
  }

  // --- JS compat: new commands ---

  @Test fun `forEach iterates and returns null`() {
    val sh = shell()
    assertEquals(TNull, sh.eval("let r = []; [1, 2, 3].forEach(x => { r = [...r, x * 2] }); r; null"))
  }

  @Test fun `concat arrays`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0), TNumber(4.0))),
      sh.eval("[1, 2].concat([3, 4])"))
  }

  @Test fun `indexOf on array`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("[10, 20, 30].indexOf(20)"))
    assertEquals(TNumber(-1.0), sh.eval("[10, 20, 30].indexOf(99)"))
  }

  @Test fun `indexOf on string`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval(""""hello".indexOf("ll")"""))
  }

  @Test fun `flatMap`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(10.0), TNumber(2.0), TNumber(20.0))),
      sh.eval("[1, 2].flatMap(x => [x, x * 10])"))
  }

  @Test fun `some`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("[1, 2, 3].some(x => x > 2)"))
    assertEquals(TBoolean(false), sh.eval("[1, 2, 3].some(x => x > 5)"))
  }

  @Test fun `every`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("[1, 2, 3].every(x => x > 0)"))
    assertEquals(TBoolean(false), sh.eval("[1, 2, 3].every(x => x > 1)"))
  }

  @Test fun `slice on array`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(2.0), TNumber(3.0))),
      sh.eval("[1, 2, 3, 4].slice(1, 3)"))
  }

  @Test fun `Array isArray`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("Array.isArray([1, 2])"))
    assertEquals(TBoolean(false), sh.eval("Array.isArray(42)"))
  }

  @Test fun `Array from`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(5.0))), sh.eval("Array.from(5)"))
    assertEquals(TArray(emptyList()), sh.eval("Array.from(null)"))
  }

  @Test fun `String constructor`() {
    val sh = shell()
    assertEquals(TString("42"), sh.eval("String(42)"))
  }

  @Test fun `Number constructor`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("""Number("42")"""))
  }

  // --- try/catch/finally/throw ---

  @Test fun `try-catch catches throw`() {
    val sh = shell()
    assertEquals(TString("got: oops"), sh.eval("""
      try { throw "oops" } catch(e) { "got: " + e }
    """))
  }

  @Test fun `try-catch catches fail`() {
    val sh = shell()
    assertEquals(TString("handled"), sh.eval("""
      try { fail("bad") } catch(e) { "handled" }
    """))
  }

  @Test fun `try-catch catches runtime errors`() {
    val sh = shell()
    assertEquals(TString("caught"), sh.eval("""
      try { null.foo } catch(e) { "caught" }
    """))
  }

  @Test fun `try without catch but with finally`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("""
      let x = 0
      try { x = 42 } finally { x = x }
      x
    """))
  }

  @Test fun `finally runs after catch`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("""
      let x = 0
      try { throw "err" } catch(e) { x = 1 } finally { x = 2 }
      x
    """))
  }

  @Test fun `finally runs on success`() {
    val sh = shell()
    assertEquals(TNumber(99.0), sh.eval("""
      let x = 0
      try { x = 1 } finally { x = 99 }
      x
    """))
  }

  @Test fun `throw with non-string value`() {
    val sh = shell()
    assertEquals(TString("caught: 42"), sh.eval("""
      try { throw 42 } catch(e) { "caught: " + e }
    """))
  }

  @Test fun `catch variable is scoped`() {
    val sh = shell()
    // e should not leak outside the catch block
    val err = assertThrows<com.iodesystems.tshell.runtime.TShellError> {
      sh.eval("""
        try { throw "x" } catch(e) { e }
        e
      """)
    }
    assertTrue(err.message!!.contains("Unknown"))
  }

  @Test fun `nested try-catch`() {
    val sh = shell()
    assertEquals(TString("inner"), sh.eval("""
      try {
        try { throw "inner" } catch(e) { e }
      } catch(e) { "outer" }
    """))
  }

  @Test fun `uncaught throw propagates`() {
    val sh = shell()
    val err = assertThrows<com.iodesystems.tshell.runtime.TShellError> {
      sh.eval("""throw "unhandled"""")
    }
    assertTrue(err.message!!.contains("unhandled"))
  }

  // --- Mutating array methods ---

  @Test fun `push appends and mutates variable`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))),
      sh.eval("let arr = [1, 2]; arr.push(3); arr"))
  }

  @Test fun `push multiple args`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))),
      sh.eval("let arr = [1]; arr.push(2, 3); arr"))
  }

  @Test fun `pop removes last and mutates`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0))),
      sh.eval("let arr = [1, 2, 3]; arr.pop(); arr"))
  }

  @Test fun `shift removes first and mutates`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(2.0), TNumber(3.0))),
      sh.eval("let arr = [1, 2, 3]; arr.shift(); arr"))
  }

  @Test fun `unshift prepends and mutates`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(0.0), TNumber(1.0), TNumber(2.0))),
      sh.eval("let arr = [1, 2]; arr.unshift(0); arr"))
  }

  @Test fun `splice removes and inserts`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(10.0), TNumber(20.0), TNumber(4.0))),
      sh.eval("let arr = [1, 2, 3, 4]; arr.splice(1, 2, 10, 20); arr"))
  }

  @Test fun `push on nested object field`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))),
      sh.eval("let obj = {items: [1, 2]}; obj.items.push(3); obj.items"))
  }

  @Test fun `push returns new array`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0))),
      sh.eval("let arr = [1]; arr.push(2)"))
  }

  @Test fun `push in loop`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(0.0), TNumber(1.0), TNumber(2.0))),
      sh.eval("""
        let arr = []
        for (let i of [0, 1, 2]) { arr.push(i) }
        arr
      """))
  }

  // --- Bitwise operations ---

  @Test fun `bitwise AND`() {
    val sh = shell()
    assertEquals(TNumber(0.0), sh.eval("5 & 2"))
    assertEquals(TNumber(1.0), sh.eval("3 & 1"))
    assertEquals(TNumber(12.0), sh.eval("255 & 12"))
  }

  @Test fun `bitwise OR`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("5 | 3"))
    assertEquals(TNumber(15.0), sh.eval("12 | 3"))
  }

  @Test fun `bitwise XOR`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("5 ^ 3"))
    assertEquals(TNumber(0.0), sh.eval("7 ^ 7"))
  }

  @Test fun `bitwise NOT`() {
    val sh = shell()
    assertEquals(TNumber(-1.0), sh.eval("~0"))
    assertEquals(TNumber(-6.0), sh.eval("~5"))
    assertEquals(TNumber(0.0), sh.eval("~-1"))
  }

  @Test fun `left shift`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("1 << 3"))
    assertEquals(TNumber(20.0), sh.eval("5 << 2"))
  }

  @Test fun `right shift`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("8 >> 2"))
    assertEquals(TNumber(-1.0), sh.eval("-1 >> 5"))
  }

  @Test fun `unsigned right shift`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("8 >>> 2"))
    // -1 >>> 0 in JS is 4294967295 (all bits set, interpreted as unsigned)
    // In Kotlin Int, -1 ushr 0 = -1 (still signed Int), but toDouble() = -1.0
    // Actually: -1 ushr 0 in Kotlin = 0xFFFFFFFF as Int = -1, so toDouble() = -1.0
    // JS returns 4294967295 because JS converts to unsigned. We match Kotlin semantics.
  }

  @Test fun `bitwise precedence matches JS`() {
    val sh = shell()
    // & binds tighter than |
    assertEquals(TNumber(7.0), sh.eval("5 | 3 & 6"))   // 5 | (3 & 6) = 5 | 2 = 7
    // ^ between & and |
    assertEquals(TNumber(7.0), sh.eval("5 | 3 ^ 1"))   // 5 | (3 ^ 1) = 5 | 2 = 7
    // shift binds tighter than comparison
    assertEquals(TBoolean(true), sh.eval("1 << 3 == 8"))
  }

  @Test fun `bitwise compound assignment AND`() {
    assertEquals(TNumber(1.0), shell().eval("let x = 3; x &= 1; x"))
  }

  @Test fun `bitwise compound assignment OR`() {
    assertEquals(TNumber(7.0), shell().eval("let x = 5; x |= 3; x"))
  }

  @Test fun `bitwise compound assignment XOR`() {
    assertEquals(TNumber(6.0), shell().eval("let x = 5; x ^= 3; x"))
  }

  @Test fun `bitwise compound assignment left shift`() {
    assertEquals(TNumber(8.0), shell().eval("let x = 1; x <<= 3; x"))
  }

  @Test fun `bitwise compound assignment right shift`() {
    assertEquals(TNumber(2.0), shell().eval("let x = 8; x >>= 2; x"))
  }

  @Test fun `bitwise compound assignment unsigned right shift`() {
    assertEquals(TNumber(2.0), shell().eval("let x = 8; x >>>= 2; x"))
  }

  @Test fun `bitwise ops coerce to int`() {
    val sh = shell()
    // Fractional parts are truncated
    assertEquals(TNumber(1.0), sh.eval("3.7 & 1.9"))
    assertEquals(TNumber(8.0), sh.eval("1.5 << 3.9"))
  }

  @Test fun `bitwise ops type error on non-number`() {
    val sh = shell()
    assertThrows<TShellError> { sh.eval("\"a\" & 1") }
    assertThrows<TShellError> { sh.eval("1 | \"b\"") }
    assertThrows<TShellError> { sh.eval("~\"x\"") }
  }

  // --- Regex character classes ---

  @Test fun `regex with character class containing slash`() {
    val sh = shell()
    // /[+\-*/]/g — dash escaped, slash allowed inside char class
    assertEquals(TArray(listOf(TString("+"), TString("-"), TString("*"), TString("/"))),
      sh.eval(""""a+b-c*d/e" |> match(/[+\-*/]/g) |> map(m => m.match)"""))
  }

  @Test fun `regex with character class containing brackets`() {
    val sh = shell()
    assertEquals(TArray(listOf(TString("["), TString("]"))),
      sh.eval(""""a[b]c" |> match(/[\[\]]/g) |> map(m => m.match)"""))
  }

  // --- Dynamic field + push (reduce_groupby pattern) ---

  @Test fun `dynamic field assignment then push`() {
    val sh = shell()
    assertEquals(TObject(linkedMapOf(
      "fruit" to TArray(listOf(TString("apple"), TString("banana")))
    )), sh.eval("""
      let acc = {}
      acc["fruit"] = []
      acc["fruit"].push("apple")
      acc["fruit"].push("banana")
      acc
    """))
  }

  @Test fun `reduce groupby pattern`() {
    val sh = shell()
    val result = sh.eval("""
      let data = [
        {type: "fruit", name: "apple"},
        {type: "veg", name: "carrot"},
        {type: "fruit", name: "banana"},
        {type: "veg", name: "pea"}
      ]
      data |> reduce((acc, item) => {
        let key = item.type
        if (!acc[key]) { acc[key] = [] }
        acc[key].push(item.name)
        return acc
      }, {})
    """)
    val obj = result as TObject
    assertEquals(TArray(listOf(TString("apple"), TString("banana"))), obj.fields["fruit"])
    assertEquals(TArray(listOf(TString("carrot"), TString("pea"))), obj.fields["veg"])
  }

  // --- Shuffle ---

  // --- Function expressions ---

  @Test fun `function expression assigned to variable`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("let double = function(x) { return x * 2 }; double(5)"))
  }

  @Test fun `anonymous function expression`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("let add = function(a, b) { return a + b }; add(2, 4)"))
  }

  @Test fun `named function expression`() {
    val sh = shell()
    assertEquals(TNumber(120.0), sh.eval("let f = function factorial(n) { if (n <= 1) return 1; return n * factorial(n - 1) }; f(5)"))
  }

  @Test fun `function expression as argument`() {
    val sh = shell()
    assertEquals(TNumber(14.0), sh.eval("[1, 2, 3] |> map(function(x) { return x * x }) |> reduce(function(a, b) { return a + b }, 0)"))
  }

  // --- Shuffle ---

  // --- Copy-on-write assignment ---

  @Test fun `nested object assignment does not mutate original`() {
    val sh = shell()
    val result = sh.eval("""
      let a = {x: {y: 1}};
      let b = a;
      b.x.y = 99;
      [a.x.y, b.x.y]
    """.trimIndent())
    // a.x.y should still be 1, b.x.y should be 99
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(99.0))), result)
  }

  @Test fun `nested array assignment does not mutate original`() {
    val sh = shell()
    val result = sh.eval("""
      let a = [[1, 2], [3, 4]];
      let b = a;
      b[0][0] = 99;
      [a[0][0], b[0][0]]
    """.trimIndent())
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(99.0))), result)
  }

  @Test fun `array auto-grow on assignment`() {
    val sh = shell()
    val result = sh.eval("""
      let arr = [];
      arr[0] = "a";
      arr[2] = "c";
      arr
    """.trimIndent())
    assertEquals(TArray(listOf(TString("a"), TNull, TString("c"))), result)
  }

  // --- Shuffle ---

  @Test fun `shuffle returns array of same length`() {
    val sh = shell()
    val result = sh.eval("[1, 2, 3, 4, 5] |> shuffle() |> sort()") as TArray
    assertEquals(5, result.elements.size)
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0), TNumber(4.0), TNumber(5.0))), result)
  }
}
