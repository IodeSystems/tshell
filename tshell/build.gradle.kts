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
    useTestNG()
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
  testImplementation(libs.testng)
  testImplementation(libs.jtokkit)
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
}

val requireSign = !rootProject.hasProperty("skipSigning")
signing {
  isRequired = requireSign
  useGpgCmd()
  sign(publishing.publications)
}
val signingTasks = tasks.withType<Sign>()
signingTasks.configureEach {
  onlyIf { requireSign }
}
tasks.withType<AbstractPublishToMaven>().configureEach {
  dependsOn(signingTasks)
}
