package com.apollographql.apollo3.network.sse

import kotlinx.serialization.Serializable

/**
 * Represents any request and response messages traveling through the SSE side channel.
 */
sealed class SseTransportMessage() {

  data class MessageType(
      val initRequest: String = "connection_init",
      val acknowledgeResponse: String = "connection_ack",
      val startRequest: String = "start",
      val startResponse: String = "start_response",

      val stopRequest: String = "stop",
      val stopResponse: String = "stop_response",

      )

  @Serializable
  data class Request(
      val type: String,
  ) : SseTransportMessage()

  @Serializable
  data class Response(
      val type: String,
  ) : SseTransportMessage()
}
