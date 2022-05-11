package com.apollographql.apollo3.network.sse

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink

open class DefaultSideChannel(
    private val sideChannelUrl: String,
    private val okHttpClient: OkHttpClient,
) :
    SseSocketEngine.SideChannel {


  open fun getBody(data: SseTransportMessage.Request): RequestBody {

    return object : RequestBody() {
      override fun contentType(): MediaType = "application/json".toMediaType()

      override fun writeTo(sink: BufferedSink) {

        Json.encodeToString(data)
            .encodeToByteArray()
            .let { sink.write(it) }
      }
    }
  }

  override fun sendData(data: SseTransportMessage.Request): SseTransportMessage.Response? {

    val body = getBody(data)

    val request = Request.Builder()
        .url(sideChannelUrl)
        .post(body)
        .build()

    return okHttpClient
        .newCall(request)
        .execute()
        .body
        ?.string()
        ?.let {
          Json.decodeFromString<SseTransportMessage.Response>(it)
        }
  }
}
