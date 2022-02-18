package featurea.content

import featurea.Bundle

interface ResourceReader {
    suspend fun createIfAbsent(resourcePath: String) {}
    suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource?
}
