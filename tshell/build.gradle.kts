import com.iodesystems.build.Antlr.antlr
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `maven-publish`
  signing
}

tasks {
  val antlrOutDir = "$projectDir/src/main/java-generated"
  val antlrInputDir = "$projectDir/src/main/antlr4"
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
      srcDir("$projectDir/src/main/java-generated")
    }
  }
}

tasks.clean {
  delete("$projectDir/src/main/java-generated")
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
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.cio)
  implementation(libs.jsoup)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
  withSourcesJar()
  withJavadocJar()
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      pom {
        name.set("tshell")
        description.set(project.description)
        url.set("https://github.com/IodeSystems/tshell")
        licenses {
          license {
            name.set("MIT License")
            url.set("https://www.opensource.org/licenses/mit-license.php")
          }
        }
        developers {
          developer {
            id.set("nthalk")
            name.set("Carl Taylor")
            email.set("carl@etaylor.me")
          }
        }
        scm {
          connection.set("scm:git:git@github.com:IodeSystems/tshell.git")
          developerConnection.set("scm:git:git@github.com:IodeSystems/tshell.git")
          url.set("https://github.com/IodeSystems/tshell")
        }
      }
    }
  }
  repositories {
    maven {
      name = "ossrh"
      val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots"
      url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
      credentials {
        username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
        password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
      }
    }
  }
}

signing {
  sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
  onlyIf { !version.toString().endsWith("SNAPSHOT") }
}
