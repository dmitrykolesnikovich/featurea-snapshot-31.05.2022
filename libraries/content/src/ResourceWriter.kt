package featurea.content

import featurea.Bundle
import featurea.runtime.Component

interface ResourceWriter {
    suspend fun accept(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle): Boolean = true
    suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle)
    suspend fun flush(bundle: Bundle) {}
}

/*convenience*/

interface ResourceWriterComponent : ResourceWriter, Component