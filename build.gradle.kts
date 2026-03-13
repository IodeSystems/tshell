import com.iodesystems.build.Antlr.antlr
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.iodesystems.tshell"
version = "0.1.0-SNAPSHOT"
description = "tshell is a sandboxed virtual shell language with TypeScript-like semantics for safe programmatic execution."

repositories {
  mavenLocal()
  mavenCentral()
}

plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  val antlrOutDir = "$rootDir/src/main/java-generated"
  val antlrInputDir = "$rootDir/src/main/antlr4"
  val lexer = register("generateLexer") {
    group = "antlr"
    val grammarFile = "TShellLexer.g4"
    val compileOnlyDepFiles = configurations.getByName("compileClasspath").asPath
    inputs.dir(antlrInputDir)
    outputs.dir(antlrOutDir)
    doLast {
      antlr(
        grammarFile = grammarFile,
        packageName = "com.iodesystems.tshell.parser",
        compileOnlyDepFiles = compileOnlyDepFiles,
        outputDir = antlrOutDir,
        inputDir = antlrInputDir
      )
    }
  }
  val parser = register("generateParser") {
    group = "antlr"
    val grammarFile = "TShellParser.g4"
    dependsOn(lexer)
    val compileOnlyDepFiles = configurations.getByName("compileClasspath").asPath
    inputs.dir(antlrInputDir)
    outputs.dir(antlrOutDir)
    doLast {
      antlr(
        grammarFile = grammarFile,
        packageName = "com.iodesystems.tshell.parser",
        compileOnlyDepFiles = compileOnlyDepFiles,
        outputDir = antlrOutDir,
        inputDir = antlrInputDir
      )
    }
  }
  compileKotlin {
    dependsOn(parser, lexer)
  }
  withType<org.gradle.jvm.tasks.Jar>().configureEach {
    dependsOn(parser)
  }
  test {
    useJUnitPlatform()
  }
}

sourceSets {
  main {
    java {
      srcDir("$rootDir/src/main/java-generated")
    }
  }
}

tasks.clean {
  delete("$rootDir/src/main/java-generated")
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    freeCompilerArgs.add("-Xjsr305=strict")
  }
}

dependencies {
  compileClasspath(libs.antlr4)
  compileClasspath(libs.antlr4.runtime)
  implementation(libs.kotlin.stdlib.jdk8)
  implementation(libs.antlr4.runtime)
  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
