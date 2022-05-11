package com.apollographql.apollo.sample.server

import com.apollographql.apollo.sample.server.routing.HelloWorldRouter
import com.apollographql.apollo.sample.server.routing.RoutingChain
import com.apollographql.apollo.sample.server.routing.SseSideChannelRouter
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json
import io.ktor.gson.*

open class KtorServerInteractor(private val port: Int = 8080) {

  fun Application.module() {
    log.info("Hello from module!")
  }

  fun invoke() {


    embeddedServer(Netty, port = port, watchPaths = listOf("classes")) {

      install(ContentNegotiation) {
//        json(Json {
//          prettyPrint = true
//          isLenient = true
//        })
        gson() {
          setPrettyPrinting()
          disableHtmlEscaping()
        }
      }

      routing {
        listOf(
            HelloWorldRouter(),
            SseSideChannelRouter(),
        )
            .let { RoutingChain(it) }
            .routing(this)
      }
    }.start(wait = true)
  }
}
