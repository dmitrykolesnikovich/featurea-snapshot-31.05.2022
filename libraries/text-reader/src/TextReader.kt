package featurea.text.reader

import featurea.Bundle
import featurea.System
import featurea.content.Resource
import featurea.content.ResourceReader
import featurea.content.ResourceReaderComponent
import featurea.content.textExtensions
import featurea.utils.hasExtension
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import

class TextReader(override val module: Module) : ResourceReaderComponent {

    private val system: System = import()

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.hasExtension(system.textExtensions)) {
            return Resource(resourcePath)
        }
        return null
    }

}
