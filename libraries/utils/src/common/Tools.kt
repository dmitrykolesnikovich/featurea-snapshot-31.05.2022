package featurea.utils

import featurea.utils.existsFile
import featurea.utils.featureaDir
import featurea.utils.isInstrumentationEnabled

object Tools {

    private val properties: LinkedHashMap<String, String> = linkedMapOf()

    operator fun set(name: String, file: String) {
        properties[name] = file
    }

    operator fun get(name: String): String? {
        val filePath = properties[name] ?: return null
        if (!existsFile(filePath)) return null
        return filePath
    }

}

fun configureTool(toolName: String) {
    Tools[toolName] = "$featureaDir/tools/$toolName/build/install/$toolName-shadow/bin/$toolName"
}
