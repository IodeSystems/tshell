package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellValue.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MathToolkitTest {

  private lateinit var shell: TShell

  @BeforeEach
  fun setup() {
    shell = TShell()
    CoreToolkit.install(shell)
    MathToolkit().install(shell)
  }

  private fun eval(code: String): String = shell.eval(code).toDisplayString()
  private fun evalNum(code: String): Double = (shell.eval(code) as TNumber).value

  @Test
  fun `constants are accessible`() {
    assertEquals(Math.PI, evalNum("Math.PI"))
    assertEquals(Math.E, evalNum("Math.E"))
  }

  @Test
  fun `sqrt works`() {
    assertEquals(5.0, evalNum("Math.sqrt(25)"))
    assertEquals(3.0, evalNum("Math.sqrt(9)"))
  }

  @Test
  fun `pow works`() {
    assertEquals(1024.0, evalNum("Math.pow(2, 10)"))
    assertEquals(8.0, evalNum("Math.pow(2, 3)"))
  }

  @Test
  fun `trig functions work`() {
    assertEquals(1.0, evalNum("Math.sin(Math.PI / 2)"), 1e-10)
    assertEquals(1.0, evalNum("Math.cos(0)"), 1e-10)
  }

  @Test
  fun `floor ceil round trunc work`() {
    assertEquals(3.0, evalNum("Math.floor(3.7)"))
    assertEquals(4.0, evalNum("Math.ceil(3.2)"))
    assertEquals(4.0, evalNum("Math.round(3.5)"))
    assertEquals(3.0, evalNum("Math.trunc(3.9)"))
    assertEquals(-3.0, evalNum("Math.trunc(-3.9)"))
  }

  @Test
  fun `abs sign work`() {
    assertEquals(5.0, evalNum("Math.abs(-5)"))
    assertEquals(-1.0, evalNum("Math.sign(-42)"))
    assertEquals(1.0, evalNum("Math.sign(42)"))
  }

  @Test
  fun `log functions work`() {
    assertEquals(1.0, evalNum("Math.log(Math.E)"), 1e-10)
    assertEquals(3.0, evalNum("Math.log2(8)"), 1e-10)
    assertEquals(2.0, evalNum("Math.log10(100)"), 1e-10)
  }

  @Test
  fun `min max work`() {
    assertEquals(1.0, evalNum("Math.min(3, 1, 2)"))
    assertEquals(3.0, evalNum("Math.max(3, 1, 2)"))
  }

  @Test
  fun `hypot works`() {
    assertEquals(5.0, evalNum("Math.hypot(3, 4)"))
  }

  @Test
  fun `random returns value in range`() {
    val v = evalNum("Math.random()")
    assertTrue(v >= 0.0 && v < 1.0)
  }

  @Test
  fun `custom global name works`() {
    val shell2 = TShell()
    CoreToolkit.install(shell2)
    MathToolkit(globalName = "M").install(shell2)
    assertEquals(5.0, (shell2.eval("M.sqrt(25)") as TNumber).value)
  }

  @Test
  fun `help shows Math guide`() {
    val help = eval("""help("Math")""")
    assertTrue(help.contains("sqrt"))
    assertTrue(help.contains("Math.PI"))
  }
}
