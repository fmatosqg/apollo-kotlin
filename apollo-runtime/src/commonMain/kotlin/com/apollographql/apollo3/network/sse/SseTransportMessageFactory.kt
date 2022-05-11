package com.apollographql.apollo3.network.sse

interface SseTransportMessageFactory {
  var type: String?

  fun build(): SseTransportMessage
}
