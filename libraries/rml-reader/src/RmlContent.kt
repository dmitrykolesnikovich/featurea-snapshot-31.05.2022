package featurea.rml.reader

import featurea.content.ResourceSchema
import featurea.runtime.*
import featurea.utils.parseProperties
import featurea.text.TextContent
import featurea.utils.readTextOrNull
import featurea.utils.toConfigPath

class RmlContent(override val module: Module) : Component {

    val textContent: TextContent = import()
    val configPackages: List<String> = container.artifact.configPackages
    private val rmlFiles = mutableMapOf<String, RmlFile>()
    private val rmlSchemas = mutableMapOf<String, ResourceSchema>()

    suspend fun findRmlSchema(resourcePackage: String): ResourceSchema {
        var rmlSchema: ResourceSchema? = rmlSchemas[resourcePackage]
        if (rmlSchema == null) {
            rmlSchema = ResourceSchema().apply {
                clearCaches()
                for (configPackage in configPackages) {
                    val configPath: String = configPackage.toConfigPath()
                    val text: String? = textContent.findTextOrNull("${configPath}/package.properties")
                    if (text != null) {
                        val properties: Map<String, String> = parseProperties(text)
                        appendProperties(properties)
                    }
                }
            }
            rmlSchemas[resourcePackage] = rmlSchema
        }
        return rmlSchema
    }

    suspend fun findRmlFile(filePath: String, source: String? = null): RmlFile {
        var rmlFile = rmlFiles[filePath]
        if (rmlFile == null) {
            val text = source ?: (textContent.findTextOrNull(filePath) ?: error("filePath: $filePath"))
            rmlFile = RmlFile().apply { init(text, filePath) { findRmlSchema(packageId) } }
            rmlFiles[filePath] = rmlFile
        }
        return rmlFile
    }

    suspend fun readRmlFile(filePath: String): RmlFile? {
        val text = textContent.system.readTextOrNull(filePath) ?: return null
        return RmlFile().apply { init(text, filePath) { findRmlSchema(packageId) } }
    }

    fun removeCachedRmlFile(filePath: String) {
        rmlFiles.remove(filePath)
    }

    fun clear() {
        rmlFiles.clear()
        rmlSchemas.clear()
    }

}


