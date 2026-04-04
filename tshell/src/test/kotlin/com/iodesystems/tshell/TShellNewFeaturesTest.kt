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

  @Test fun `match returns first match with groups`() {
    val sh = shell()
    // String pattern: JS non-global semantics — first match only
    val result = sh.eval("\"abc123def456\" |> match(\"[0-9]+\")") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("123"), result.elements[0])
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

  @Test fun `bitwise OR is unsupported error`() {
    val sh = shell()
    val e = assertThrows<TShellError> { sh.eval("5 | 3") }
    assertTrue(e.message!!.contains("not supported"))
    assertTrue(e.message!!.contains("|>"))
    assertTrue(e.message!!.contains("||"))
  }

  @Test fun `bitwise XOR operator is unsupported error`() {
    val sh = shell()
    val e = assertThrows<TShellError> { sh.eval("5 ^ 3") }
    assertTrue(e.message!!.contains("not supported"))
    assertTrue(e.message!!.contains("**"))
    assertTrue(e.message!!.contains("|."))
  }

  @Test fun `bitwise XOR via function`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("xor(5, 3)"))
    assertEquals(TNumber(0.0), sh.eval("xor(7, 7)"))
  }

  @Test fun `bitwise OR via pipe-colon`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("5 |: 3"))
  }

  @Test fun `bitwise XOR via pipe-dot`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("5 |. 3"))
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

  @Test fun `bitwise precedence — shift vs comparison`() {
    val sh = shell()
    // shift binds tighter than comparison
    assertEquals(TBoolean(true), sh.eval("1 << 3 == 8"))
  }

  @Test fun `bitwise compound assignment AND`() {
    assertEquals(TNumber(1.0), shell().eval("let x = 3; x &= 1; x"))
  }

  @Test fun `bitwise compound assignment OR is unsupported error`() {
    val e = assertThrows<TShellError> { shell().eval("let x = 5; x |= 3; x") }
    assertTrue(e.message!!.contains("not supported"))
  }

  @Test fun `bitwise compound assignment XOR is unsupported error`() {
    val e = assertThrows<TShellError> { shell().eval("let x = 5; x ^= 3; x") }
    assertTrue(e.message!!.contains("not supported"))
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
    // Global match returns flat array of match strings (JS-compatible)
    assertEquals(TArray(listOf(TString("+"), TString("-"), TString("*"), TString("/"))),
      sh.eval(""""a+b-c*d/e" |> match(/[+\-*/]/g)"""))
  }

  @Test fun `regex with character class containing brackets`() {
    val sh = shell()
    // Global match returns flat array of match strings (JS-compatible)
    assertEquals(TArray(listOf(TString("["), TString("]"))),
      sh.eval(""""a[b]c" |> match(/[\[\]]/g)"""))
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

  // --- JS reference semantics ---

  @Test fun `nested object assignment mutates shared reference (JS semantics)`() {
    val sh = shell()
    val result = sh.eval("""
      let a = {x: {y: 1}};
      let b = a;
      b.x.y = 99;
      [a.x.y, b.x.y]
    """.trimIndent())
    // JS: a and b are the same object, so both see the mutation
    assertEquals(TArray(listOf(TNumber(99.0), TNumber(99.0))), result)
  }

  @Test fun `nested array assignment mutates shared reference (JS semantics)`() {
    val sh = shell()
    val result = sh.eval("""
      let a = [[1, 2], [3, 4]];
      let b = a;
      b[0][0] = 99;
      [a[0][0], b[0][0]]
    """.trimIndent())
    // JS: a and b are the same array, so both see the mutation
    assertEquals(TArray(listOf(TNumber(99.0), TNumber(99.0))), result)
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

  // --- Sort direction ---

  @Test fun `sort desc reverses numeric array`() {
    val sh = shell()
    val result = sh.eval("""[3, 1, 2] |> sort("desc")""") as TArray
    assertEquals(TArray(listOf(TNumber(3.0), TNumber(2.0), TNumber(1.0))), result)
  }

  @Test fun `sort asc is same as default sort`() {
    val sh = shell()
    val result = sh.eval("""[3, 1, 2] |> sort("asc")""") as TArray
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0))), result)
  }

  @Test fun `sort descending on strings`() {
    val sh = shell()
    val result = sh.eval("""["banana", "apple", "cherry"] |> sort("desc")""") as TArray
    assertEquals(TArray(listOf(TString("cherry"), TString("banana"), TString("apple"))), result)
  }

  // --- Raw template strings ---

  @Test fun `raw template string preserves backslashes`() {
    val sh = shell()
    assertEquals(TString("C:\\Users\\foo"), sh.eval("""r`C:\Users\foo`"""))
  }

  @Test fun `raw template string with interpolation`() {
    val sh = shell()
    assertEquals(TString("Hello world, path=C:\\Users"), sh.eval("""let name = "world"; r`Hello ${'$'}{name}, path=C:\Users`"""))
  }

  @Test fun `raw template string preserves backslash n literally`() {
    val sh = shell()
    assertEquals(TString("line1\\nline2"), sh.eval("""r`line1\nline2`"""))
  }

  @Test fun `regular template string still unescapes`() {
    val sh = shell()
    assertEquals(TString("line1\nline2"), sh.eval("""`line1\nline2`"""))
  }

  @Test fun `raw template string multiline`() {
    val sh = shell()
    val result = sh.eval("r`first\nsecond`")
    assertEquals(TString("first\nsecond"), result)
  }

  // --- r as identifier (no regression) ---

  @Test fun `r as variable name`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("let r = 42; r"))
  }

  @Test fun `r as function name`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("function r(x) { return x * 2 }; r(5)"))
  }

  @Test fun `r as parameter name`() {
    val sh = shell()
    assertEquals(TNumber(7.0), sh.eval("let f = (r) => r + 1; f(6)"))
  }

  @Test fun `r as object key`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("let o = {r: 1}; o.r"))
  }

  @Test fun `r as named argument`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("function f(r) { return r }; f(r: 3)"))
  }

  @Test fun `r called then raw template on next line`() {
    val sh = shell()
    assertEquals(TString("hello"), sh.eval("let r = (x) => x; r(r`hello`)"))
  }

  @Test fun `r assigned then used in expression`() {
    val sh = shell()
    assertEquals(TNumber(15.0), sh.eval("let r = 5; r * 3"))
  }

  // --- Quoted object keys (JSON compat) ---

  @Test fun `object with quoted string keys`() {
    val sh = shell()
    assertEquals(TString("apple"), sh.eval("""{"type": "fruit", "name": "apple"}.name"""))
  }

  @Test fun `array of objects with quoted keys and reduce`() {
    val sh = shell()
    val result = sh.eval("""
      [{"type":"fruit","name":"apple"},{"type":"veg","name":"carrot"},{"type":"fruit","name":"banana"},{"type":"veg","name":"pea"}]
      |> reduce((acc, item) => {
          let t = item.type;
          acc[t] = (acc[t] || []).concat([item.name]);
          return acc;
      }, {})
    """.trimIndent())
    val obj = result as TObject
    val fruit = (obj.fields["fruit"] as TArray).elements.map { (it as TString).value }
    val veg = (obj.fields["veg"] as TArray).elements.map { (it as TString).value }
    assertEquals(listOf("apple", "banana"), fruit)
    assertEquals(listOf("carrot", "pea"), veg)
  }

  // --- Top-level return ---

  @Test fun `top-level return yields value`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("return 3 + 5"))
  }

  @Test fun `top-level return from curried function`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("""
      let curriedAdd = (a) => (b) => a + b;
      let add5 = curriedAdd(5);
      return add5(3);
    """))
  }

  // --- Raw quoted strings r"..." / r'...' ---

  @Test fun `raw double-quoted string preserves backslashes`() {
    val sh = shell()
    assertEquals(TString("C:\\Users\\admin"), sh.eval("""r"C:\Users\admin""""))
  }

  @Test fun `raw single-quoted string preserves backslashes`() {
    val sh = shell()
    assertEquals(TString("C:\\Users\\admin"), sh.eval("r'C:\\Users\\admin'"))
  }

  @Test fun `raw string does not process escape sequences`() {
    val sh = shell()
    assertEquals(TString("hello\\nworld\\t!"), sh.eval("""r"hello\nworld\t!""""))
  }

  @Test fun `raw string with regex pattern`() {
    val sh = shell()
    assertEquals(TString("\\d+\\.\\d+"), sh.eval("""r"\d+\.\d+""""))
  }

  @Test fun `raw string in array`() {
    val sh = shell()
    val result = sh.eval("""[r"C:\path\one", r"D:\path\two"]""")
    val arr = result as TArray
    assertEquals(TString("C:\\path\\one"), arr.elements[0])
    assertEquals(TString("D:\\path\\two"), arr.elements[1])
  }

  @Test fun `raw string with split for Windows paths`() {
    val sh = shell()
    assertEquals(TString("file.txt"), sh.eval("""r"C:\Users\admin\file.txt" |> split(r"\") |> last()"""))
  }

  // --- evalExported with vars ---

  @Test fun `vars are available in code`() {
    val sh = shell()
    assertEquals(TString("hello world"), sh.evalExported("input", mapOf("input" to TString("hello world"))))
  }

  @Test fun `vars with different types`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.evalExported("n * 2", mapOf("n" to TNumber(21.0))))
  }

  @Test fun `vars with arrays`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.evalExported("len(items)", mapOf(
      "items" to TArray(listOf(TString("a"), TString("b"), TString("c")))
    )))
  }

  @Test fun `vars with objects`() {
    val sh = shell()
    assertEquals(TString("Alice"), sh.evalExported("user.name", mapOf(
      "user" to TObject(mapOf("name" to TString("Alice"), "age" to TNumber(30.0)))
    )))
  }

  @Test fun `vars do not leak to globals`() {
    val sh = shell()
    sh.evalExported("x + 1", mapOf("x" to TNumber(5.0)))
    assertThrows<TShellError> { sh.eval("x") }
  }

  @Test fun `vars cannot be reassigned in code`() {
    val sh = shell()
    assertThrows<TShellError> { sh.evalExported("let x = 99", mapOf("x" to TNumber(5.0))) }
  }

  @Test fun `vars avoid double escaping for paths`() {
    val sh = shell()
    assertEquals(TString("C:\\Users\\foo"), sh.evalExported("path", mapOf("path" to TString("C:\\Users\\foo"))))
  }

  // --- Array constructor and fill ---

  @Test fun `Array constructor creates array of n nulls`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNull, TNull, TNull)), sh.eval("Array(3)"))
  }

  @Test fun `Array constructor with fill`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(0.0), TNumber(0.0), TNumber(0.0))), sh.eval("Array(3).fill(0)"))
  }

  @Test fun `Array constructor preserves namespace methods`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("Array.isArray([1, 2])"))
  }

  @Test fun `Array constructor with zero`() {
    val sh = shell()
    assertEquals(TArray(emptyList()), sh.eval("Array(0)"))
  }

  @Test fun `Array constructor with no args`() {
    val sh = shell()
    assertEquals(TArray(emptyList()), sh.eval("Array()"))
  }

  @Test fun `fill with start and end params`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(0.0), TNumber(9.0), TNumber(9.0), TNumber(0.0))),
      sh.eval("[0, 0, 0, 0] |> fill(9, 1, 3)"))
  }

  @Test fun `fill with only start param`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(0.0), TNumber(7.0), TNumber(7.0))),
      sh.eval("[0, 0, 0] |> fill(7, 1)"))
  }

  @Test fun `fill via pipe syntax`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(1.0), TNumber(1.0))),
      sh.eval("[0, 0] |> fill(1)"))
  }

  @Test fun `calling a plain object without __call throws`() {
    val sh = shell()
    assertThrows<TShellError> { sh.eval("let obj = {a: 1}; obj()") }
  }

  @Test fun `object with __call is callable`() {
    val sh = shell()
    assertEquals(TNumber(42.0), sh.eval("""
      let obj = { __call: (x) => x * 2, label: "doubler" };
      obj(21)
    """))
  }

  @Test fun `callable object in method chain`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("""
      let ns = { create: { __call: (n) => n * 2 } };
      ns.create(5)
    """))
  }

  @Test fun `Array from with length object`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNull, TNull, TNull)), sh.eval("Array.from({ length: 3 })"))
  }

  @Test fun `Array from with length object and map function`() {
    val sh = shell()
    assertEquals(TArray(listOf(TNumber(0.0), TNumber(1.0), TNumber(2.0))),
      sh.eval("Array.from({ length: 3 }, (_, i) => i)"))
  }

  @Test fun `Array from with length and fill pattern for 2D arrays`() {
    val sh = shell()
    assertEquals(TNumber(4.0), sh.eval("""
      function lcsLength(s1, s2) {
        let m = s1.length;
        let n = s2.length;
        let dp = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0));
        for (let i = 1; i <= m; i++) {
          for (let j = 1; j <= n; j++) {
            if (s1[i-1] === s2[j-1]) {
              dp[i][j] = dp[i-1][j-1] + 1;
            } else {
              dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
            }
          }
        }
        return dp[m][n];
      }
      let s1 = 'ABCBDAB';
      let s2 = 'BDCAB';
      lcsLength(s1, s2);
    """))
  }

  @Test fun `Array fill map pattern for 2D arrays`() {
    val sh = shell()
    assertEquals(TNumber(4.0), sh.eval("""
      function lcsLength(s1, s2) {
        let m = s1.length;
        let n = s2.length;
        let dp = Array(m + 1).fill(null).map(() => Array(n + 1).fill(0));
        for (let i = 1; i <= m; i++) {
          for (let j = 1; j <= n; j++) {
            if (s1[i-1] === s2[j-1]) {
              dp[i][j] = dp[i-1][j-1] + 1;
            } else {
              dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
            }
          }
        }
        return dp[m][n];
      }
      let s1 = 'ABCBDAB';
      let s2 = 'BDCAB';
      lcsLength(s1, s2);
    """))
  }

  // --- Numeric object key coercion ---

  @Test fun `numeric key read on object`() {
    val sh = shell()
    assertEquals(TString("a"), sh.eval("""let obj = {"0": "a", "1": "b"}; obj[0]"""))
  }

  @Test fun `numeric key write on object`() {
    val sh = shell()
    assertEquals(TString("x"), sh.eval("""let obj = {}; obj[0] = "x"; obj[0]"""))
  }

  @Test fun `numeric key round-trips with string key - num to str`() {
    val sh = shell()
    assertEquals(TString("v"), sh.eval("""let obj = {}; obj[0] = "v"; obj["0"]"""))
  }

  @Test fun `numeric key round-trips with string key - str to num`() {
    val sh = shell()
    assertEquals(TString("w"), sh.eval("""let obj = {}; obj["1"] = "w"; obj[1]"""))
  }

  // --- Postfix increment/decrement in expressions ---

  @Test fun `postfix increment returns old value`() {
    val sh = shell()
    assertEquals(TNumber(0.0), sh.eval("let i = 0; let x = i++; x"))
  }

  @Test fun `postfix increment mutates variable`() {
    val sh = shell()
    assertEquals(TNumber(1.0), sh.eval("let i = 0; i++; i"))
  }

  @Test fun `postfix increment in array index`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("let arr = [10,20,30]; let i = 0; arr[i++]"))
  }

  @Test fun `postfix decrement returns old value`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("let i = 5; let x = i--; x"))
  }

  @Test fun `postfix decrement mutates variable`() {
    val sh = shell()
    assertEquals(TNumber(4.0), sh.eval("let i = 5; i--; i"))
  }

  @Test fun `postfix increment in for loop`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("""
      let sum = 0;
      let i = 0;
      while (i < 5) {
        sum += i++;
      }
      sum
    """))
  }

  @Test fun `standalone postfix increment still works`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("let i = 0; i++; i++; i++; i"))
  }

  // --- Exponentiation operator ** ---

  @Test fun `exponentiation basic`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("2 ** 3"))
  }

  @Test fun `exponentiation right-associative`() {
    val sh = shell()
    // 2 ** 3 ** 2 = 2 ** 9 = 512 (right-associative)
    assertEquals(TNumber(512.0), sh.eval("2 ** 3 ** 2"))
  }

  @Test fun `exponentiation with multiplication`() {
    val sh = shell()
    // 3 * 2 ** 3 = 3 * 8 = 24 (** binds tighter than *)
    assertEquals(TNumber(24.0), sh.eval("3 * 2 ** 3"))
  }

  @Test fun `exponentiation assign`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval("let x = 2; x **= 3; x"))
  }

  // --- number.toString() and toFixed() ---

  @Test fun `number toString`() {
    val sh = shell()
    assertEquals(TString("42"), sh.eval("(42).toString()"))
  }

  @Test fun `number toString on float`() {
    val sh = shell()
    assertEquals(TString("3.14"), sh.eval("(3.14).toString()"))
  }

  @Test fun `number toFixed`() {
    val sh = shell()
    assertEquals(TString("3.14"), sh.eval("(3.14159).toFixed(2)"))
  }

  @Test fun `boolean toString`() {
    val sh = shell()
    assertEquals(TString("true"), sh.eval("true.toString()"))
  }

  // --- Braceless loops ---

  @Test fun `braceless while`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("let i = 0; while (i < 5) i++; i"))
  }

  @Test fun `braceless for`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("let sum = 0; for (let i = 0; i < 5; i++) sum += i; sum"))
  }

  @Test fun `braceless for-of`() {
    val sh = shell()
    assertEquals(TNumber(6.0), sh.eval("let sum = 0; for (let x of [1,2,3]) sum += x; sum"))
  }

  @Test fun `nested braceless for-if`() {
    val sh = shell()
    // The pattern that failed in the LCS benchmark
    assertEquals(TNumber(3.0), sh.eval("""
      let count = 0;
      for (let i = 0; i < 5; i++)
        if (i > 1) count++;
      count
    """.trimIndent()))
  }

  // --- Comma expressions ---

  @Test fun `comma expression in parens returns last`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("(1, 2, 3)"))
  }

  @Test fun `comma expression evaluates all and returns last`() {
    val sh = shell()
    assertEquals(TString("b"), sh.eval("""("a", "b")"""))
  }

  // --- Bare ref detection ---

  @Test fun `bare ref in block is an error when non-terminal`() {
    val sh = shell()
    val e = assertThrows<TShellError> {
      sh.eval("""
        function f(x) {
          x
          let y = 1
        }
        f(5)
      """.trimIndent())
    }
    assertTrue(e.message!!.contains("no effect"))
    assertTrue(e.message!!.contains("return x"))
  }

  @Test fun `bare ref as last statement in block is fine`() {
    val sh = shell()
    // x as last statement of the if block is the return value
    assertEquals(TNumber(5.0), sh.eval("function f(x) { if (x > 0) { x } else { 0 } }; f(5)"))
  }

  @Test fun `bare ref at top level is fine`() {
    val sh = shell()
    // Last expression at program level is the return value — not an error
    assertEquals(TNumber(42.0), sh.eval("let x = 42; x"))
  }

  @Test fun `function call in block is not a bare ref`() {
    val sh = shell()
    // fn() has side effects — not flagged
    assertEquals(TNumber(1.0), sh.eval("function f() { print(1) }; f()"))
  }

  // --- Numeric object keys ---

  @Test fun `numeric object keys`() {
    val sh = shell()
    val result = sh.eval("{0: \"a\", 1: \"b\"}") as TObject
    assertEquals(TString("a"), result.fields["0"])
    assertEquals(TString("b"), result.fields["1"])
  }

  // --- Assignment as expression ---

  @Test fun `assignment in arrow body`() {
    val sh = shell()
    assertEquals(TNumber(99.0), sh.eval("""
      let obj = {a: 1};
      let fn = x => obj.a = x;
      fn(99)
    """.trimIndent()))
  }

  // --- Destructured params ---

  @Test fun `array destructured arrow param`() {
    val sh = shell()
    assertEquals(TNumber(10.0), sh.eval("[[1,2],[3,4]] |> map(([a,b]) => a + b) |> reduce((s,x) => s+x, 0)"))
  }

  // --- Map on objects ---

  @Test fun `map over object values`() {
    val sh = shell()
    val result = sh.eval("{a: 1, b: 2} |> map(v => v * 10)") as TObject
    assertEquals(TNumber(10.0), result.fields["a"])
    assertEquals(TNumber(20.0), result.fields["b"])
  }

  // --- .len on arrays ---

  @Test fun `array dot len as callable`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("[1,2,3].len()"))
    // .len is a function (truthy), .length is the number directly
    assertEquals(TNumber(3.0), sh.eval("[1,2,3].length"))
    assertTrue(sh.eval("[1,2,3].len").isTruthy())
  }

  // --- lastIndexOf ---

  @Test fun `lastIndexOf on string`() {
    val sh = shell()
    assertEquals(TNumber(8.0), sh.eval(""" "foo bar foo" |> lastIndexOf("foo") """))
  }
}
