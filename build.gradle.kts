plugins {
  kotlin("jvm") apply false
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
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

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
      username.set(findProperty("sonatypeUsername") as String?)
      password.set(findProperty("sonatypePassword") as String?)
    }
  }
}
