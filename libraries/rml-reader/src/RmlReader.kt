package featurea.rml.reader

import featurea.Bundle
import featurea.System
import featurea.content.Resource
import featurea.content.ResourceReader
import featurea.content.rmlExtensions
import featurea.utils.hasExtension
import featurea.runtime.Container

class RmlReader(container: Container) : ResourceReader {

    private val system: System = container.import()

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.hasExtension(system.rmlExtensions)) {
            return Resource(resourcePath)
        }
        return null
    }

}
