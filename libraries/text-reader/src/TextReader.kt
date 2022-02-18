package featurea.text.reader

import featurea.Bundle
import featurea.System
import featurea.content.Resource
import featurea.content.ResourceReader
import featurea.content.textExtensions
import featurea.hasExtension
import featurea.runtime.Container

class TextReader(container: Container) : ResourceReader {

    private val system: System = container.import()

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.hasExtension(system.textExtensions)) {
            return Resource(resourcePath)
        }
        return null
    }

}
