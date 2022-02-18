package featurea.shader.writer

import featurea.Bundle
import featurea.content.ResourceTag
import featurea.content.ResourceWriter
import featurea.content.shaderExtension
import featurea.extension
import featurea.runtime.Container

class ShaderWriter(container: Container) : ResourceWriter {

    /*private val system: System = import()*/

    override suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        if (value.extension != shaderExtension) return

        /*
        val resourcePath = value
        val resourceBundlePath = "$userHomePath/$SHADER_CACHE_PATH/$resourcePath"
        val shaderFile = system.findFileOrNull(resourcePath.replace(".shader", "-js.shader")) ?: system.findFile(resourcePath)
        val shaderBundleFile = File(resourceBundlePath)
        if (shaderBundleFile.exists()) shaderBundleFile.delete()
        shaderBundleFile.createNewFileAndDirs()
        val text = shaderFile.readText()
        shaderBundleFile.writeText(text)
        */
    }

}
