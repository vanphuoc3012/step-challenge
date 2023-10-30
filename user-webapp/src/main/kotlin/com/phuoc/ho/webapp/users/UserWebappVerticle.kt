package com.phuoc.ho.webapp.users

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import org.slf4j.LoggerFactory

class UserWebappVerticle : AbstractVerticle() {

  companion object {
    val HTTP_PORT = System.getProperty("http.port", "8080")
        .toInt()
    val log = LoggerFactory.getLogger(UserWebappVerticle::class.java)
  }

  override fun start() {
    val router = Router.router(vertx)

    router.route()
        .handler(StaticHandler.create("webroot/assets"))
    router.get("/*")
        .handler { ctx -> ctx.reroute("/index.html") }

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(HTTP_PORT)
        .onSuccess { log.info("Success start Webapp server at port: $HTTP_PORT") }
        .onFailure { log.error("Whops. Failed to start Webapp Server") }
  }
}

fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(UserWebappVerticle())
}