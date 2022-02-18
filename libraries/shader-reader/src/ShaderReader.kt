package featurea.shader.reader

import featurea.*
import featurea.content.Resource
import featurea.content.ResourceReader
import featurea.content.shaderExtension
import featurea.runtime.Container

class ShaderReader(container: Container) : ResourceReader {

    private val system: System = container.import()

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.extension != shaderExtension) return null
        if (bundle == null) return Resource(resourcePath)

        val transpiledResourcePath = resourcePath.replaceSuffix(".shader", "Transpiled.shader")
        when {
            system.existsFile(transpiledResourcePath) -> return Resource(transpiledResourcePath)
            else -> return Resource(resourcePath)
        }
    }

}
