repositories {
  mavenLocal()
  mavenCentral()
  gradlePluginPortal()
}

plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.gradle.kotlin.plugin)
  implementation(libs.kotlin.serialization.plugin)
}
