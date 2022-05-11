package com.apollographql.apollo.sample

import com.apollographql.apollo.sample.server.KtorServerInteractor
import com.apollographql.apollo.sample.server.routing.HelloWorldRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Starts and stops a ktor test in time for running E2E test.
 *
 * TODO: there's big potential for needing to run these tests with --no-parallel
 */
open class KtorTest {

  private var job: Job? = null

  private val port = 8080

  val sideChannelUrl = "http://localhost:$port"

  @Before
  fun setup() {
    job = CoroutineScope(Dispatchers.IO).launch {
          KtorServerInteractor(port = port).invoke()
        }

    runBlocking {
      delay(300) // we need to wait for server to start
    }
  }

  @After
  fun tearDown() {
    job?.cancel()
  }

  @Test
  fun `Given ktor is running When connect Then get 'hello'`() {

    val client = OkHttpClient.Builder().build()

    val request = Request.Builder().url("$sideChannelUrl/${HelloWorldRouter.PATH_HELLO_WORLD}").build()

    client.newCall(request).execute().let {
          Assert.assertEquals(200, it.code)
          Assert.assertEquals(HelloWorldRouter.PAYLOAD_HELLO_WORLD, it.body?.string())
        }

  }
}