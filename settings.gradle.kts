pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "tshell-parent"

include("tshell")
include("tshell-playwright")
include("tshell-sql")
include("tshell-sample-koog")
