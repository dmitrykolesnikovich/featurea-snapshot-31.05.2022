package featurea.content

import featurea.Bundle

// todo migrate to Kotlin 1.6.20 to apply `context(ContainerContext)`
interface ResourceWriter {
    suspend fun accept(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle): Boolean = true
    suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle)
    suspend fun flush(bundle: Bundle) {}
}
