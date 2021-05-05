package com.apollographql.apollo3.compiler

import com.apollographql.apollo3.graphql.ast.GQLDocument
import com.apollographql.apollo3.graphql.ast.GQLFragmentDefinition
import com.apollographql.apollo3.graphql.ast.GQLOperationDefinition
import com.apollographql.apollo3.graphql.ast.toGraphQLExecutableDefinitions
import com.apollographql.apollo3.graphql.ast.toGraphQLSchema
import com.apollographql.apollo3.graphql.ast.toUtf8
import com.apollographql.apollo3.graphql.ast.withTypenameWhenNeeded
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@Suppress("UNUSED_PARAMETER")
@RunWith(Parameterized::class)
class TypenameTest(val name: String, private val graphQLFile: File) {

  @Test
  fun testTypename() {
    val schemaFile = File("src/test/graphql/schema.sdl")
    val schema = schemaFile.toGraphQLSchema()

    val definitions = graphQLFile.toGraphQLExecutableDefinitions(schema)

    val documentWithTypename = GQLDocument(
        definitions = definitions.map {
          when (it) {
            is GQLOperationDefinition -> it.withTypenameWhenNeeded(schema)
            is GQLFragmentDefinition -> it.withTypenameWhenNeeded(schema)
            else -> it
          }
        },
        filePath = null
    ).toUtf8()

    val expectedFile = File(graphQLFile.parentFile, "${name}.with_typename")


    if (TestUtils.shouldUpdateTestFixtures()) {
      expectedFile.writeText(documentWithTypename)
    } else {
      assertThat(documentWithTypename).isEqualTo(expectedFile.readText())
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun data(): Collection<Array<Any>> {
      return File("src/test/typename/")
          .walk()
          .toList()
          .filter { it.isFile }
          .filter { it.extension == "graphql" }
          .sortedBy { it.name }
          .map { arrayOf(it.nameWithoutExtension, it) }
    }
  }
}