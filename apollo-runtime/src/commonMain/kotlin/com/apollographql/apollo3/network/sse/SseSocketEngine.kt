package com.apollographql.apollo3.network.sse


import kotlinx.coroutines.flow.Flow

interface SseSocketEngine {

  fun createSideChannel(url: String): SideChannel

  fun createSseChannel(url: String): MainChannel


  interface SideChannel {
    /**
     * This represents a typical REST Get or Post
     */
    fun sendData(data: SseTransportMessage.Request): SseTransportMessage.Response?
  }

  interface MainChannel {

    fun getData(): Flow<SsePayload>

    data class SsePayload(
        val data: String? = null,
        val id: String? = null,
        val type: String? = null,
    )
  }
}
