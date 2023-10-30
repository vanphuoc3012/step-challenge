apply(plugin = "org.gradle.test-retry")

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  val vertxVersion = project.extra["vertxVersion"]
  val junit5Version = project.extra["junit5Version"]
  val logbackClassicVersion = project.extra["logbackClassicVersion"]
  val restAssuredVersion = project.extra["restAssuredVersion"]
  val assertjVersion = project.extra["assertjVersion"]
  val testContainersVersion = project.extra["testContainersVersion"]

  implementation(kotlin("stdlib-jdk8"))

  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-lang-kotlin-coroutines:${vertxVersion}")
  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("io.vertx:vertx-web-client:$vertxVersion")
  implementation("io.vertx:vertx-auth-jwt:$vertxVersion")
  implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

  testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("io.vertx:vertx-junit5-rx-java2:$vertxVersion")
  testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
  testImplementation("org.assertj:assertj-core:$assertjVersion")
}

application {
  mainClass.set("com.phuoc.ho.publicapi.PublicApiVerticle")
}

tasks.test {
  useJUnitPlatform()
  retry {
    maxRetries.set(1)
  }
}