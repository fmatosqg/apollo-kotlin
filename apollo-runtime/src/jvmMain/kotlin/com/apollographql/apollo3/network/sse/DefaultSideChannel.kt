package com.apollographql.apollo3.network.sse

import com.google.gson.Gson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink

//@ExperimentalSerializationApi
open class DefaultSideChannel(
    private val sideChannelUrl: String,
    private val okHttpClient: OkHttpClient,
    private val messageType: SseTransportMessage.MessageType = SseTransportMessage.MessageType(),
) :
    SseSocketEngine.SideChannel {

  private val gson = Gson() // TODO remove


  open fun getBody(data: SseTransportMessage.Request): RequestBody {

    return object : RequestBody() {
      override fun contentType(): MediaType = "application/json".toMediaType()

      override fun writeTo(sink: BufferedSink) {

//        Json.encodeToString(data)
//            .encodeToByteArray()
//            .let { sink.write(it) }

        // TODO remove
        gson.toJson(data)
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
        .let {
//          Json.decodeFromString<SseTransportMessage.Response>(it) // TODO find a way to compile this
          gson.fromJson(it, SseTransportMessage.Response::class.java) // TODO remove this
        }

  }

}