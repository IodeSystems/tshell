package com.iodesystems.tshell

import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class JsCompatProbeTest {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  private fun probe(label: String, code: String) {
    val sh = shell()
    try {
      val result = sh.eval(code)
      println("PROBE [$label]: OK -> $result")
    } catch (e: Throwable) {
      println("PROBE [$label]: ${e::class.simpleName} -> ${e.message}")
    }
  }

  @Test fun `01 const x = 5`() = probe("const x = 5", "const x = 5")
  @Test fun `02 var x = 5`() = probe("var x = 5", "var x = 5")
  @Test fun `03 console log`() = probe("console.log(\"hi\")", """console.log("hi")""")
  @Test fun `04 Array isArray`() = probe("Array.isArray([1])", "Array.isArray([1])")
  @Test fun `05 string length`() = probe("\"hello\".length", """"hello".length""")
  @Test fun `06 string toUpperCase`() = probe("\"hello\".toUpperCase()", """"hello".toUpperCase()""")
  @Test fun `07 array map`() = probe("[1,2,3].map(x => x * 2)", "[1,2,3].map(x => x * 2)")
  @Test fun `08 array filter`() = probe("[1,2,3].filter(x => x > 1)", "[1,2,3].filter(x => x > 1)")
  @Test fun `09 array length`() = probe("[1,2,3].length", "[1,2,3].length")
  @Test fun `10 JSON parse`() = probe("JSON.parse", """JSON.parse('{"a":1}')""")
  @Test fun `11 JSON stringify`() = probe("JSON.stringify", "JSON.stringify({a: 1})")
  @Test fun `12 Math floor`() = probe("Math.floor(3.5)", "Math.floor(3.5)")
  @Test fun `13 Object keys`() = probe("Object.keys({a: 1})", "Object.keys({a: 1})")
  @Test fun `14 typeof operator`() = probe("typeof x === \"number\"", """typeof x === "number"""")
  @Test fun `15 instanceof`() = probe("x instanceof Array", "let x = 1; x instanceof Array")
  @Test fun `16 new Date`() = probe("new Date()", "new Date()")
  @Test fun `17 class Foo`() = probe("class Foo {}", "class Foo {}")
  @Test fun `18 import`() = probe("import x from 'y'", "import x from 'y'")
  @Test fun `19 async function`() = probe("async function f() {}", "async function f() {}")
  @Test fun `20 try catch`() = probe("try { x } catch(e) {}", "try { x } catch(e) {}")
  @Test fun `21 throw`() = probe("throw new Error(\"x\")", """throw new Error("x")""")
  @Test fun `22 switch`() = probe("switch", "let x = 1; switch(x) { case 1: break; }")
  @Test fun `23 triple equals`() = probe("x === y", "let x = 1; let y = 1; x === y")
  @Test fun `24 fn keyword`() = probe("fn f() {}", "fn f() {}")
  @Test fun `25 const arrow return`() = probe("const arrow return", "const f = (x) => { return x }; f(42)")
  @Test fun `26 Array from`() = probe("Array.from([1,2])", "Array.from([1,2])")
  @Test fun `27 forEach`() = probe("[1,2,3].forEach", "[1,2,3].forEach(x => print(x))")
  @Test fun `28 string includes`() = probe("\"hello\".includes(\"ell\")", """"hello".includes("ell")""")
  @Test fun `29 string split`() = probe("\"hello\".split(\"\")", """"hello".split("")""")
  @Test fun `30 object spread`() = probe("{...obj, key: val}", "let obj = {a: 1}; let val = 2; {...obj, key: val}")
  @Test fun `31 C-style for`() = probe("C-style for", "let s = 0; for (let i = 0; i < 5; i++) { s = s + i }; s")
  @Test fun `32 const in for-of`() = probe("const in for-of", "let s = 0; for (const x of [1,2]) { s = s + x }; s")
  @Test fun `33 destructuring`() = probe("destructuring", "let {a, b} = {a: 1, b: 2}; a + b")
  @Test fun `34 Promise all`() = probe("Promise.all([])", "Promise.all([])")
  @Test fun `35 setTimeout`() = probe("setTimeout", "setTimeout(() => {}, 100)")
  @Test fun `36 String constructor`() = probe("String(42)", "String(42)")
  @Test fun `37 Number constructor`() = probe("Number(\"42\")", """Number("42")""")
  @Test fun `38 Boolean constructor`() = probe("Boolean(1)", "Boolean(1)")
  @Test fun `39 leading dot`() = probe("leading dot .map", ".map(x => x)")
  @Test fun `40 array push`() = probe("[1,2].push(3)", "[1,2].push(3)")
  @Test fun `41 delete`() = probe("delete obj.key", "let obj = {key: 1}; delete obj.key")
  @Test fun `42 in operator`() = probe("x in obj", """let obj = {a: 1}; "a" in obj""")
  @Test fun `43 void 0`() = probe("void 0", "void 0")
  @Test fun `44 null coalescing`() = probe("null ?? \"default\"", """null ?? "default"""")
  @Test fun `45 optional chaining`() = probe("x?.y?.z", "let x = {y: {z: 42}}; x?.y?.z")
}
