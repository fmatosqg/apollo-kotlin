package com.apollographql.apollo.sample

import com.apollographql.apollo.sample.server.routing.SseSideChannelRouter
import com.apollographql.apollo3.network.sse.DefaultSideChannel
import com.apollographql.apollo3.network.sse.SseTransportMessage
import com.apollographql.apollo3.network.sse.SseClientTransportMessageFactory
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient
import org.junit.Test

class SideChannelTest : KtorTest() {

  private val okHttpClient = OkHttpClient.Builder()
      .build()

  private val messageType = SseTransportMessage.MessageType()

  private val sideChannel = DefaultSideChannel(
      okHttpClient = okHttpClient,
      sideChannelUrl = sideChannelUrl + "/" + SseSideChannelRouter.SIDE_CHANNEL_PATH, // TODO do it neatly with some URL class
  )

  private val messageFactory = SseClientTransportMessageFactory(messageType = messageType)

  @Test
  fun `Given healthy server When send init message Then get ack response`() {

    messageFactory
        .setTypeToInit()
        .build()
        .let { sideChannel.sendData(it) }
        .let {
          assertEquals(messageType.acknowledgeResponse, it?.type)
        }
  }
}
