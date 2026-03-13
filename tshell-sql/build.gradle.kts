plugins {
  kotlin("jvm")
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
  // JDBC is part of the JDK — no dependency needed for the API.
  // Drivers are the consumer's responsibility (compileOnly examples below).
  // compileOnly("com.h2database:h2:2.3.232")
  // compileOnly("org.postgresql:postgresql:42.7.4")

  testImplementation(libs.testng)
  testImplementation("com.h2database:h2:2.3.232")
}

tasks.test {
  useTestNG()
}
