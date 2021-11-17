import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.api.ObjectIdGenerator
import com.apollographql.apollo3.cache.normalized.api.ObjectIdGeneratorContext
import com.apollographql.apollo3.cache.normalized.api.TypePolicyObjectIdGenerator
import com.apollographql.apollo3.testing.readFile

fun readJson(name: String) = readFile("../models-fixtures/json/$name")

/**
 * A [ObjectIdGenerator] that always uses the "id" field if it exists and delegates to [TypePolicyObjectIdGenerator] else
 *
 * It will coerce Int, Floats and other types to String using [toString]
 */
object IdObjectIdGenerator : ObjectIdGenerator {
  override fun cacheKeyForObject(obj: Map<String, Any?>, context: ObjectIdGeneratorContext): CacheKey? {
    return obj["id"]?.toString()?.let { CacheKey(it) } ?: TypePolicyObjectIdGenerator.cacheKeyForObject(obj, context)
  }
}