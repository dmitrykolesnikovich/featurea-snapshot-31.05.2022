package featurea.shader.reader

import featurea.Bundle
import featurea.System
import featurea.content.Resource
import featurea.content.ResourceReaderComponent
import featurea.content.shaderExtension
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.existsFile
import featurea.utils.extension
import featurea.utils.replaceSuffix

class ShaderReader(override val module: Module) : ResourceReaderComponent {

    private val system: System = import()

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
