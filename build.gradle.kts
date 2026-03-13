plugins {
  kotlin("jvm") apply false
}

allprojects {
  group = "com.iodesystems.tshell"
  version = "0.1.0-SNAPSHOT"
  description = "tshell is a sandboxed virtual shell language with TypeScript-like semantics for safe programmatic execution."

  repositories {
    mavenLocal()
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    jvmToolchain(21)
  }
}
