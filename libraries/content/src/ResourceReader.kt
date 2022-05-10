package featurea.content

import featurea.Bundle
import featurea.runtime.Component

interface ResourceReader {
    suspend fun createIfAbsent(resourcePath: String) {}
    suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource?
}

/*convenience*/

interface ResourceReaderComponent : ResourceReader, Component
