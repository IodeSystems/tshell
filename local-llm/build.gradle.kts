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
  mainClass.set("com.iodesystems.tshell.localllm.MainKt")
}

dependencies {
  implementation(project(":"))
  implementation(project(":playwright-tools"))
  implementation(libs.koog.agents)
  implementation(libs.clikt)
  implementation(libs.ktor.client.cio)
}
