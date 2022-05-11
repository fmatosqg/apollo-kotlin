plugins {
  application
  kotlin("jvm")
  kotlin("plugin.serialization") version "1.6.10"
}

dependencies {

  ///////////////////////////
  // implementation
  implementation("com.apollographql.apollo3:apollo-runtime")

  // ktor
  implementation(groovy.util.Eval.x(project, "x.dep.ktor.serverCore"))
  implementation(groovy.util.Eval.x(project, "x.dep.ktor.serverNetty"))
  implementation(groovy.util.Eval.x(project, "x.dep.ktor.serialization"))
  implementation(groovy.util.Eval.x(project, "x.dep.logback"))
  implementation("io.ktor:ktor-gson:1.6.7") // TODO remove all gson references
//  implementation("io.ktor:ktor-client-gson:1.6.7")
//  implementation("io.ktor:ktor-client-serialization:1.6.7")

  ////////////////////////////
  // test
  testImplementation(groovy.util.Eval.x(project, "x.dep.kotlinJunit"))
  testImplementation(groovy.util.Eval.x(project, "x.dep.ktor.clientOkHttp"))

}

application {
  applicationName = "GraphQl SSE Subscriptions server in Ktor"
  mainClass.set("com.apollographql.apollo.sample.server.MainKt")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true") // hot reload when running gradle in continuous mode
}
