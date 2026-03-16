package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class TShellRegexTest {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  // --- Regex literal parsing ---

  @Test fun `simple regex literal`() {
    val sh = shell()
    val result = sh.eval("/abc/")
    assertTrue(result is TRegex)
    assertEquals((result as TRegex).pattern, "abc")
    assertEquals(result.flags, "")
  }

  @Test fun `regex with flags`() {
    val sh = shell()
    val result = sh.eval("/[0-9]+/gi")
    assertTrue(result is TRegex)
    assertEquals((result as TRegex).pattern, "[0-9]+")
    assertEquals(result.flags, "gi")
  }

  @Test fun `regex with escaped slash`() {
    val sh = shell()
    val result = sh.eval("""/foo\/bar/""")
    assertTrue(result is TRegex)
    assertEquals((result as TRegex).pattern, "foo\\/bar")
  }

  @Test fun `regex display string`() {
    val sh = shell()
    val result = sh.eval("/abc/gi")
    assertEquals(result.toDisplayString(), "/abc/gi")
  }

  @Test fun `regex is truthy`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("/abc/ ? true : false"))
  }

  @Test fun `regex typeof`() {
    val sh = shell()
    assertEquals(TString("regex"), sh.eval("typeof(/abc/)"))
  }

  // --- Disambiguation: regex vs division ---

  @Test fun `division still works`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("10 / 2"))
    assertEquals(TNumber(2.5), sh.eval("let a = 10; a / 4"))
  }

  @Test fun `division chain`() {
    val sh = shell()
    assertEquals(TNumber(2.0), sh.eval("12 / 2 / 3"))
  }

  @Test fun `division after parens`() {
    val sh = shell()
    assertEquals(TNumber(3.0), sh.eval("(9) / 3"))
  }

  @Test fun `division after bracket access`() {
    val sh = shell()
    assertEquals(TNumber(5.0), sh.eval("let a = [10]; a[0] / 2"))
  }

  @Test fun `regex after assignment`() {
    val sh = shell()
    val result = sh.eval("let r = /abc/; r")
    assertTrue(result is TRegex)
    assertEquals((result as TRegex).pattern, "abc")
  }

  @Test fun `regex in function call`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval(""""hello123" |> test(/[0-9]+/)"""))
  }

  @Test fun `regex after pipe`() {
    val sh = shell()
    val result = sh.eval(""""abc123" |> match(/([a-z]+)([0-9]+)/)""")
    // JS-compatible: non-global returns [fullMatch, group1, group2, ...]
    assertEquals(result, TArray(listOf(TString("abc123"), TString("abc"), TString("123"))))
  }

  // --- match with regex ---

  @Test fun `match with string pattern backward compat`() {
    val sh = shell()
    val result = sh.eval(""""abc123def456" |> match("[0-9]+")""")
    assertEquals(result, TArray(listOf(TString("123"), TString("456"))))
  }

  @Test fun `match with regex returns capture groups`() {
    val sh = shell()
    // JS-compatible: non-global returns [fullMatch, group1, group2, ...]
    val result = sh.eval(""""abc123" |> match(/([a-z]+)([0-9]+)/)""")
    assertEquals(result, TArray(listOf(TString("abc123"), TString("abc"), TString("123"))))
  }

  @Test fun `match with global regex finds all`() {
    val sh = shell()
    // JS-compatible: global returns flat array of match strings
    val result = sh.eval(""""abc123def456" |> match(/[a-z]+/g)""")
    assertEquals(result, TArray(listOf(TString("abc"), TString("def"))))
  }

  @Test fun `match without global returns first only`() {
    val sh = shell()
    // JS-compatible: non-global returns [fullMatch] (no groups in this regex)
    val result = sh.eval(""""abc123def456" |> match(/[a-z]+/)""")
    assertEquals(result, TArray(listOf(TString("abc"))))
  }

  // --- replace with regex ---

  @Test fun `replace with string backward compat`() {
    val sh = shell()
    assertEquals(TString("hello tshell"), sh.eval(""""hello world" |> replace("world", "tshell")"""))
  }

  @Test fun `replace with regex`() {
    val sh = shell()
    assertEquals(TString("123 abc"), sh.eval(""""abc 123" |> replace(/([a-z]+) ([0-9]+)/, "$2 $1")"""))
  }

  @Test fun `replace with regex global`() {
    val sh = shell()
    assertEquals(TString("X1X X2X"), sh.eval(""""a1b a2b" |> replace(/[a-z]/g, "X")"""))
  }

  // --- split with regex ---

  @Test fun `split with string backward compat`() {
    val sh = shell()
    assertEquals(TArray(listOf(TString("a"), TString("b"), TString("c"))), sh.eval(""""a,b,c" |> split(",")"""))
  }

  @Test fun `split with regex`() {
    val sh = shell()
    val result = sh.eval(""""a, b,  c" |> split(/,\s*/)""")
    assertEquals(result, TArray(listOf(TString("a"), TString("b"), TString("c"))))
  }

  // --- test ---

  @Test fun `test with regex true`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval(""""hello123" |> test(/[0-9]+/)"""))
  }

  @Test fun `test with regex false`() {
    val sh = shell()
    assertEquals(TBoolean(false), sh.eval(""""hello" |> test(/[0-9]+/)"""))
  }

  @Test fun `test with string pattern`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval("""test("hello123", "[0-9]+")"""))
  }

  @Test fun `test case insensitive`() {
    val sh = shell()
    assertEquals(TBoolean(true), sh.eval(""""HELLO" |> test(/hello/i)"""))
  }

  // --- Set operations ---

  @Test fun `difference`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(3.0))),
      sh.eval("difference([1, 2, 3, 4], [2, 4])")
    )
  }

  @Test fun `difference with strings`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TString("a"), TString("c"))),
      sh.eval("""difference(["a", "b", "c"], ["b"])""")
    )
  }

  @Test fun `intersection`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(2.0), TNumber(3.0))),
      sh.eval("intersection([1, 2, 3], [2, 3, 4])")
    )
  }

  @Test fun `union`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TNumber(1.0), TNumber(2.0), TNumber(3.0), TNumber(4.0))),
      sh.eval("union([1, 2, 3], [2, 3, 4])")
    )
  }

  @Test fun `union preserves order`() {
    val sh = shell()
    assertEquals(
      TArray(listOf(TString("a"), TString("b"), TString("c"))),
      sh.eval("""union(["a", "b"], ["b", "c"])""")
    )
  }

  // --- Edge cases ---

  @Test fun `regex with complex pattern`() {
    val sh = shell()
    val result = sh.eval("""/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/""")
    assertTrue(result is TRegex)
  }

  @Test fun `regex in filter pipeline`() {
    val sh = shell()
    val result = sh.eval("""["abc", "123", "def", "456"] |> filter(s => s |> test(/[0-9]+/))""")
    assertEquals(result, TArray(listOf(TString("123"), TString("456"))))
  }

  @Test fun `match with regex multiline flag`() {
    val sh = shell()
    // JS-compatible: non-global returns [fullMatch]
    val result = sh.eval(""""line1\nline2\nline3" |> match(/^line[0-9]+/m)""")
    assertEquals(result, TArray(listOf(TString("line1"))))
  }
}
