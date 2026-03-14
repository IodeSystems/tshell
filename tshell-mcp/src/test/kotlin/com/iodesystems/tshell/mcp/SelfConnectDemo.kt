package com.iodesystems.tshell.mcp

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.toolkit.CoreToolkit

fun main() {
  val projectRoot = java.io.File(System.getProperty("user.dir"))
  val cliPath = java.io.File(projectRoot, "tshell-cli/build/install/tshell-cli/bin/tshell-cli").absolutePath
  require(java.io.File(cliPath).exists()) { "CLI not found at $cliPath" }

  val shell = TShell()
  CoreToolkit.install(shell)

  val toolkit = McpToolkit(
    servers = mapOf(
      "remote" to McpToolkit.McpServerConfig(
        command = listOf(cliPath),
        label = "remote tshell",
      ),
    ),
  )

  toolkit.use {
    it.install(shell)

    println("=== help() ===")
    println((shell.eval("help()") as TShellValue.TString).value)

    println("\n=== remote.eval(\"1 + 2\") ===")
    println(shell.eval("""remote.eval("1 + 2")""").toDisplayString())

    println("\n=== remote.eval(\"[3,1,2] |> sort()\") ===")
    println(shell.eval("""remote.eval("[3,1,2] |> sort()")""").toDisplayString())

    println("\n=== remote.help() ===")
    println(shell.eval("""remote.help()""").toDisplayString())
  }
}
