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

// Allow users to extend the classpath via TSHELL_CLASSPATH env var (e.g. JDBC drivers)
tasks.named<CreateStartScripts>("startScripts") {
  doLast {
    // Unix script: append $TSHELL_CLASSPATH after the generated CLASSPATH line
    unixScript.writeText(
      unixScript.readText().replace(
        Regex("^(CLASSPATH=.*)$", RegexOption.MULTILINE),
        "$1\nif [ -n \"\\\$TSHELL_CLASSPATH\" ]; then CLASSPATH=\"\\\$CLASSPATH:\\\$TSHELL_CLASSPATH\"; fi"
      )
    )
    // Windows script: append %TSHELL_CLASSPATH% after the generated CLASSPATH line
    windowsScript.writeText(
      windowsScript.readText().replace(
        Regex("^(set CLASSPATH=.*)$", RegexOption.MULTILINE),
        "$1\r\nif defined TSHELL_CLASSPATH set CLASSPATH=%CLASSPATH%;%TSHELL_CLASSPATH%"
      )
    )
  }
}

dependencies {
  implementation(project(":tshell"))
  implementation(project(":tshell-mcp"))
  implementation(project(":tshell-playwright"))
  implementation(project(":tshell-sql"))
  implementation(libs.clikt)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
}
