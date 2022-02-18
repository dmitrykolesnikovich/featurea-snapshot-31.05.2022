package featurea.config

import featurea.content.ResourceSchema
import featurea.rml.reader.RmlContent
import featurea.toConfigPath
import featurea.toFilePath
import featurea.toIdPath

class ConfigContent(val rmlContent: RmlContent) {

    private val configs = mutableMapOf<String, Config>()
    private val configFiles = mutableMapOf<String, ConfigFile?>()

    suspend fun findValueOrNull(configPath: String): String? {
        val name: String = configPath.toFilePath()
        val key: String? = configPath.toIdPath()
        val config: Config = findConfig(name)
        return config[key]
    }

    suspend fun findConfig(name: String): Config {
        var config: Config? = configs[name]
        if (config == null) {
            config = Config(name).apply {
                for (configPackage in rmlContent.configPackages) {
                    val configPath = configPackage.toConfigPath()
                    val configFilePath = "${configPath}/${name}.properties"
                    val configFile: ConfigFile? = findConfigFileOrNull(configFilePath)
                    if (configFile != null) {
                        files.add(configFile)
                        if (rmlSchema == null) {
                            rmlSchema = rmlContent.findRmlSchema(configPackage)
                        }
                    }
                }
                if (rmlSchema == null) {
                    rmlSchema = ResourceSchema()
                }
                initProperties()
            }
            configs[name] = config
        }
        return config
    }

    suspend fun findConfigFileOrNull(filePath: String): ConfigFile? {
        if (!configFiles.containsKey(filePath)) {
            val text: String? = rmlContent.textContent.findTextOrNull(filePath)
            if (text != null) {
                configFiles[filePath] = ConfigFile(text, filePath)
            }
        }
        return configFiles[filePath]
    }

    fun clearCache() {
        configs.clear()
        configFiles.clear()
    }

}

/*convenience*/

suspend fun ConfigContent.importsOf(name: String): Map<String, String> {
    for (configPackage in rmlContent.configPackages) {
        val configPath = configPackage.toConfigPath()
        val configFilePath = "$configPath/$name.properties"
        val configFile = findConfigFileOrNull(configFilePath)
        if (configFile != null) return configFile.imports
    }
    error("name: $name")
}
