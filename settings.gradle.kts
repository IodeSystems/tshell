pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "tshell-parent"

include("tshell")
include("tshell-mcp")
include("tshell-cli")
include("tshell-playwright")
include("tshell-sql")
include("tshell-graph")
include("tshell-sample-koog")
