package com.phuoc.ho.publicapi

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.predicate.ResponsePredicate
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import org.slf4j.LoggerFactory

class PublicApiVerticle : AbstractVerticle() {
  private val HTTP_PORT = System.getProperty("http.port", "4000")
      .toInt()
  val log = LoggerFactory.getLogger(PublicApiVerticle::class.java)
  lateinit var webClient: WebClient
  lateinit var jwtAuth: JWTAuth

  override fun start() {
    webClient = WebClient.create(vertx)

    val publickey = CryptoHelper.publickey()
    val privatekey = CryptoHelper.privatekey()

    jwtAuth = JWTAuth.create(vertx, JWTAuthOptions()
        .addPubSecKey(PubSecKeyOptions().setAlgorithm("RS256")
                          .setBuffer(publickey))
        .addPubSecKey(PubSecKeyOptions().setAlgorithm("RS256")
                          .setBuffer(privatekey))
    )

    val jwtAuthHandler = JWTAuthHandler.create(jwtAuth)

    val router = Router.router(vertx)
    val bodyHandler = BodyHandler.create()
    router.post()
        .handler(bodyHandler)
    router.put()
        .handler(bodyHandler)
    val prefix = "/api/v1"

    router.route()
        .handler(CorsHandler.create()
                     .allowedHeaders(allowHeaders())
                     .allowedMethods(allowMethods())
                     .addOrigin("*"))

    fun createPath(path: String): String = if (path.startsWith("/")) "$prefix$path" else "$prefix/$path"

    router.post(createPath("/register"))
        .handler(::register)
    router.post(createPath("token"))
        .handler(::token)
    router.get(createPath("/:username"))
        .handler(::fetchUser)
    router.get(createPath("/:username/:year/:month"))
        .handler(jwtAuthHandler)
        .handler(::checkUser)
        .handler(::monthlySteps)


    vertx.createHttpServer()
        .requestHandler(router)
        .listen(HTTP_PORT)
        .onSuccess { log.debug("Api-Public Server started with port $HTTP_PORT") }
        .onFailure { log.debug("Whoppss, failed to start server", it) }
  }

  private fun allowHeaders(): Set<String> {
    val allowedHeaders: MutableSet<String> = HashSet()
    allowedHeaders.add("x-requested-with")
    allowedHeaders.add("Access-Control-Allow-Origin")
    allowedHeaders.add("origin")
    allowedHeaders.add("Content-Type")
    allowedHeaders.add("accept")
    allowedHeaders.add("Authorization")
    return allowedHeaders
  }

  private fun allowMethods(): Set<HttpMethod> {
    val allowedMethods: MutableSet<HttpMethod> = HashSet()
    allowedMethods.add(HttpMethod.GET)
    allowedMethods.add(HttpMethod.POST)
    allowedMethods.add(HttpMethod.OPTIONS)
    allowedMethods.add(HttpMethod.PUT)
    return allowedMethods
  }

  private fun monthlySteps(context: RoutingContext) {
    val deviceId = context.user()
        .principal()
        .getString("deviceId")
    val year = context.pathParam("year")
    val month = context.pathParam("month")
    webClient.get(3001, "localhost", "/$deviceId/$year/$month")
        .`as`(BodyCodec.jsonObject())
        .send()
        .onSuccess { res -> forwardJsonOrStatusCode(context, res) }
        .onFailure { err -> sendBadGateway(context, err) }
  }

  private fun checkUser(context: RoutingContext) {
    val username = context.user()
        .principal()
        .getString("sub")
    if (!context.pathParam("username")
          .equals(username)
    ) {
      sendStatusCode(context, 403)
    } else {
      context.next()
    }
  }

  private fun fetchUser(context: RoutingContext) {
    webClient.get(3000, "localhost", "/${context.pathParam("username")}")
        .`as`(BodyCodec.jsonObject())
        .send()
        .onSuccess { res -> forwardJsonOrStatusCode(context, res) }
        .onFailure { err -> sendBadGateway(context, err) }
  }

  private fun forwardJsonOrStatusCode(context: RoutingContext, res: HttpResponse<JsonObject>) {
    if (res.statusCode() != 200) {
      sendStatusCode(context, res.statusCode())
    } else {
      context.response()
          .putHeader("Content-Type", "application/json")
          .end(res.body()
                   .encode())
    }
  }

  private fun token(context: RoutingContext) {
    val payload = context.body()
        .asJsonObject()
    val username = payload.getString("username")
    val password = payload.getString("password")
    webClient.post(3000, "localhost", "/authenticate")
        .`as`(BodyCodec.jsonObject())
        .expect(ResponsePredicate.SC_SUCCESS)
        .sendJson(payload)
        .flatMap { fetchUserDetails(username) }
        .map {
          it.body()
              .getString("deviceId")
        }
        .map { makeJwtToken(username, it) }
        .onSuccess { token ->
          sendToken(context, token)
        }
        .onFailure { err -> handleAuthError(context, err) }
  }

  private fun sendToken(context: RoutingContext, token: String) {
    context.response()
        .putHeader("Content-Type", "application/jwt")
        .end(token)
  }

  private fun handleAuthError(context: RoutingContext, err: Throwable) {
    log.debug("Auth fail: username: {}", context.body()
        .asJsonObject()
        .getString("username"), err)
    context.fail(401)
  }

  private fun makeJwtToken(username: String, deviceId: String): String {
    val claims = JsonObject().put("deviceId", deviceId)
    val jwtOptions = JWTOptions().setAlgorithm("RS256")
        .setExpiresInSeconds(10_800)
        .setIssuer("10k-steps-challenge")
        .setSubject(username)
    return jwtAuth.generateToken(claims, jwtOptions)
  }

  private fun fetchUserDetails(username: String): Future<HttpResponse<JsonObject>> {
    return webClient.get(3000, "localhost", "/username")
        .expect(ResponsePredicate.SC_SUCCESS)
        .`as`(BodyCodec.jsonObject())
        .send()
  }

  private fun register(context: RoutingContext) {
    webClient.post(3000, "localhost", "/register")
        .putHeader("Content-Type", "application/json")
        .sendJson(context.body()
                      .asJsonObject())
        .onSuccess { res ->
          sendStatusCode(context, res.statusCode())
        }
        .onFailure { err -> sendBadGateway(context, err) }
  }

  private fun sendBadGateway(context: RoutingContext, err: Throwable) {
    log.debug("Whoopss: ", err)
    context.fail(502)
  }

  private fun sendStatusCode(context: RoutingContext, statusCode: Int) {
    context.response()
        .setStatusCode(statusCode)
        .end()
  }

}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(PublicApiVerticle::class.java, DeploymentOptions())
}