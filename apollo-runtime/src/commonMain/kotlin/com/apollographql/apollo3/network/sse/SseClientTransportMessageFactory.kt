package com.apollographql.apollo3.network.sse

/**
 * Creates client requests
 */
open class SseClientTransportMessageFactory(private val messageType: SseTransportMessage.MessageType = SseTransportMessage.MessageType()): SseTransportMessageFactory {

  override var type: String? = null

  fun setTypeToInit(): SseClientTransportMessageFactory {
    type = messageType.initRequest
    return this
  }

  fun setTypeToStart(): SseClientTransportMessageFactory {
    type = messageType.startRequest
    return this
  }

  override fun build(): SseTransportMessage.Request {
    checkNotNull(type) {
      "Apollo: 'type' is required"
    }

    return SseTransportMessage
        .Request(type = type!!)
  }
}
