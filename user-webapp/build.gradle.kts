import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

apply(plugin = "com.github.node-gradle.node")

val main = "com.phuoc.ho.webapps.users.UserWebappVerticle"

tasks.named("yarn_build") {
  dependsOn("yarn_install")
}

dependencies {
  val vertxVersion = project.extra["vertxVersion"]
  val logbackClassicVersion = project.extra["logbackClassicVersion"]

  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
}

application {
  mainClass.set(main)
}

tasks.register("copyVueDist") {
  dependsOn("yarn_build")
  doLast {
    copy {
      from("dist/")
      into("src/main/resources/webroot")
    }
  }
}


tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
  }
  mergeServiceFiles()
}

//tasks.named("build") {
//  dependsOn("copyVueDist")
//}

tasks.processResources {
  dependsOn("copyVueDist")
}

tasks.named("clean") {
  delete("$projectDir/dist")
  delete("$projectDir/src/main/resources/webroot/assets")
}