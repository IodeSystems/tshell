package com.iodesystems.tshell.mcp

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.annotations.Test

class McpSelfConnectTest {

  @Test
  fun `connect to tshell-cli and show help`() {
    // Resolve from project root (test CWD is the submodule dir)
    val projectRoot = java.io.File(System.getProperty("user.dir")).parentFile
    val cliPath = java.io.File(projectRoot, "tshell-cli/build/install/tshell-cli/bin/tshell-cli").absolutePath
    require(java.io.File(cliPath).exists()) { "CLI not found at $cliPath — run :tshell-cli:installDist first" }

    val shell = TShell()
    CoreToolkit.install(shell)

    val toolkit = McpToolkit(
      servers = mapOf(
        "remote" to McpToolkit.McpServerConfig(
          command = listOf(cliPath),
          label = "remote tshell (self-connect)",
        ),
      ),
    )

    toolkit.use {
      it.install(shell)

      // Show all commands including remote ones
      val help = shell.eval("help()") as TShellValue.TString
      println(help.value)

      // Call remote eval through tshell
      val result = shell.eval("""remote.eval("1 + 2")""")
      println("\nremote.eval(\"1 + 2\") = ${result.toDisplayString()}")

      // Call remote help
      val remoteHelp = shell.eval("""remote.help()""")
      println("\nremote.help() = ${remoteHelp.toDisplayString()}")
    }
  }
}
