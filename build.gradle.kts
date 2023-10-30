import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.9.0"
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("org.gradle.test-retry") version "1.2.0" apply false
  id("com.github.node-gradle.node") version "7.0.1" apply false
}

repositories {
  mavenCentral()
}

allprojects {
  extra["vertxVersion"] = "4.4.6"
  extra["junit5Version"] = "5.10.0"
  extra["restAssuredVersion"] = "5.3.2"
  extra["logbackClassicVersion"] = "1.4.11"
  extra["assertjVersion"] = "3.24.2"
  extra["testContainersVersion"] = "1.19.1"
}

subprojects {
  apply(plugin = "kotlin")
  apply(plugin = "application")
  apply(plugin = "com.github.johnrengelman.shadow")
  repositories {
    mavenCentral()
  }

  val compileKotlin: KotlinCompile by tasks
  compileKotlin.kotlinOptions.jvmTarget = "11"

  kotlin {
    jvmToolchain(11)
  }
}