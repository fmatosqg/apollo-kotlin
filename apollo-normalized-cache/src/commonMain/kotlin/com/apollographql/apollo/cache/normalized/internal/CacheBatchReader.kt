package com.apollographql.apollo.cache.normalized.internal

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.internal.Utils.shouldSkip
import com.apollographql.apollo.cache.CacheHeaders
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.CacheReference
import com.apollographql.apollo.exception.FieldMissingException
import com.apollographql.apollo.exception.ObjectMissingException

/**
 * Reads [rootFieldSets] starting at [rootKey] from [readableStore]
 *
 * This is a resolver that solves the "N+1" problem by batching all SQL queries at a given depth
 * It respects skip/include directives
 *
 * Returns the data in [toMap]
 */
class CacheBatchReader(
    private val readableStore: ReadableStore,
    private val rootKey: String,
    private val variables: Operation.Variables,
    private val cacheKeyResolver: CacheKeyResolver,
    private val cacheHeaders: CacheHeaders,
    private val rootFieldSets: List<ResponseField.FieldSet>
) {
  private val cacheKeyBuilder = RealCacheKeyBuilder()

  class PendingReference(
      val key: String,
      val fieldSets: List<ResponseField.FieldSet>
  )

  private val data = mutableMapOf<String, Map<String, Any?>>()

  private val pendingReferences = mutableListOf<PendingReference>()

  private fun ResponseField.Type.isObject(): Boolean = when (this) {
    is ResponseField.Type.NotNull -> ofType.isObject()
    is ResponseField.Type.Named.Object -> true
    else -> false
  }

  fun toMap(): Map<String, Any?> {
    pendingReferences.add(
        PendingReference(
            rootKey,
            rootFieldSets
        )
    )

    while (pendingReferences.isNotEmpty()) {
      val records = readableStore.read(pendingReferences.map { it.key }, cacheHeaders).associateBy { it.key }

      val copy = pendingReferences.toList()
      pendingReferences.clear()
      copy.forEach { pendingReference ->
        val record = records[pendingReference.key] ?: throw ObjectMissingException(pendingReference.key)

        val fieldSet = pendingReference.fieldSets.firstOrNull { it.typeCondition == record["__typename"] }
            ?: pendingReference.fieldSets.first { it.typeCondition == null }

        val map = fieldSet.responseFields.mapNotNull {
          if (it.shouldSkip(variables.valueMap())) {
            return@mapNotNull null
          }

          val type = it.type
          val value = if (type.isObject()) {
            val cacheKey = cacheKeyResolver.fromFieldArguments(it, variables)
            if (cacheKey != CacheKey.NO_KEY ) {
              // user provided a lookup
              CacheReference(cacheKey.key)
            } else {
              // no key provided
              val fieldName = cacheKeyBuilder.build(it, variables)
              if (!record.containsKey(fieldName)) {
                throw FieldMissingException(record.key, fieldName, cacheKeyBuilder.build(it, variables))
              }
              record[fieldName]
            }
          } else {
            val fieldName = cacheKeyBuilder.build(it, variables)
            if (!record.containsKey(fieldName)) {
              throw FieldMissingException(record.key, fieldName, cacheKeyBuilder.build(it, variables))
            }
            record[fieldName]
          }

          value.registerCacheReferences(it.fieldSets)

          it.responseName to value
        }.toMap()

        data[record.key] = map
      }
    }

    @Suppress("UNCHECKED_CAST")
    return data[rootKey].resolveCacheReferences() as Map<String, Any?>
  }

  private fun Any?.registerCacheReferences(fieldSets: List<ResponseField.FieldSet>) {
    when (this) {
      is CacheReference -> {
        pendingReferences.add(PendingReference(key, fieldSets))
      }
      is List<*> -> {
        forEach {
          it.registerCacheReferences(fieldSets)
        }
      }
    }
  }

  private fun Any?.resolveCacheReferences(): Any? {
    return when (this) {
      is CacheReference -> {
        data[key].resolveCacheReferences()
      }
      is List<*> -> {
        map {
          it.resolveCacheReferences()
        }
      }
      is Map<*, *> -> {
        // This will traverse Map custom scalars but this is ok as it shouldn't contain any CacheReference
        mapValues { it.value.resolveCacheReferences() }
      }
      else -> this
    }
  }
}
