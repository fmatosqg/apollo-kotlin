package com.apollographql.apollo3.graphql.ast


/**
 * All the issues that can be collected while analyzing a graphql document
 */
sealed class Issue(
    val message: String,
    val sourceLocation: SourceLocation,
    val severity: Severity,
) {
  /**
   * A grammar error
   */
  class ParsingError(message: String, sourceLocation: SourceLocation) : Issue(message, sourceLocation, Severity.ERROR)

  /**
   * A validation error
   */
  class ValidationError(message: String, sourceLocation: SourceLocation) : Issue(message, sourceLocation, Severity.ERROR)

  /**
   * A deprecated field/enum is used
   */
  class DeprecatedUsage(message: String, sourceLocation: SourceLocation) : Issue(message, sourceLocation, Severity.WARNING)

  /**
   * An unknown directive was found.
   *
   * In a perfect world everyone uses SDL schemas and we can validate directives but in this world, a lot of users rely
   * on introspection schemas that do not contain directives. If this happens, we pass them through without validation.
   */
  class UnknownDirective(message: String, sourceLocation: SourceLocation) : Issue(message, sourceLocation, Severity.WARNING)

  /**
   * A variable is unused
   */
  class UnusedVariable(message: String, sourceLocation: SourceLocation) : Issue(message, sourceLocation, Severity.WARNING)

  /**
   * Upper case fields are not supported as Kotlin doesn't allow a property name with the same name as a nested class.
   * If this happens, the easiest solution is to add an alias with a lower case first letter.
   *
   * This error is an Apollo Android specific error
   */
  class UpperCaseField(message: String, sourceLocation: SourceLocation) : Issue(message, sourceLocation, Severity.ERROR)

  enum class Severity {
    WARNING,
    ERROR,
  }
}

fun List<Issue>.checkNoErrors() {
  val error = firstOrNull { it.severity == Issue.Severity.ERROR }
  if (error != null) {
    throw SourceAwareException(
        error.message,
        error.sourceLocation
    )
  }
}