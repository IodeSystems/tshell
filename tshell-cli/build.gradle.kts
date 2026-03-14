plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  application
}

group = "com.iodesystems.tshell"
version = "0.1.0-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
}

kotlin {
  jvmToolchain(21)
}

application {
  mainClass.set("com.iodesystems.tshell.cli.MainKt")
}

dependencies {
  implementation(project(":tshell"))
  implementation(project(":tshell-mcp"))
  implementation(project(":tshell-playwright"))
  implementation(project(":tshell-sql"))
  implementation(libs.clikt)
  implementation(libs.kotlinx.coroutines.core)
}
