package featurea.config

import featurea.*
import featurea.content.Resource
import featurea.content.ResourcePropertyDelegate
import featurea.content.ResourceReader
import featurea.content.propertiesExtension
import featurea.utils.*
import kotlin.text.endsWith

class ConfigReader(val configContent: ConfigContent) : ResourceReader {

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.extension == propertiesExtension) {
            val configPath: String = findConfigPath(resourcePath) ?: return null
            val name: String = configPath.pathWithoutExtension
            val config: Config = configContent.findConfig(name)
            val files: List<String> = config.files.map { it.path }
            return Resource(files) {
                Properties("configPath" to configPath)
            }
        }
        return null
    }

    /*internals*/

    private fun findConfigPath(value: String): String? {
        if (value.endsWith("-desktop.properties")) {
            if (System.target != SystemTarget.DESKTOP) {
                return null
            }
            val result: String = value.replaceSuffix("-desktop.properties", ".properties")
            return result
        }
        return value
    }

}

/*properties*/

val Resource.configPath: String by ResourcePropertyDelegate("configPath") { error("configPath") }
