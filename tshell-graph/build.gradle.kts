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

  testImplementation(libs.testng)
}

tasks.test {
  useTestNG()
  testLogging {
    showStandardStreams = true
  }
}
