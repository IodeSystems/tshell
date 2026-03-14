plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
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

dependencies {
  implementation(project(":tshell"))
  implementation(libs.mcp.kotlin.sdk)
  implementation(libs.ktor.client.cio)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.testng)
}

tasks.test {
  useTestNG()
  testLogging {
    showStandardStreams = true
  }
}

tasks.register<JavaExec>("selfConnectDemo") {
  dependsOn(":tshell-cli:installDist")
  classpath = sourceSets["test"].runtimeClasspath
  mainClass.set("com.iodesystems.tshell.mcp.SelfConnectDemoKt")
  workingDir = rootProject.projectDir
}
