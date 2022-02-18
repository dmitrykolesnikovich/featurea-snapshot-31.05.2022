package featurea.config

import featurea.parseProperties

class ConfigFile(val source: String, val path: String) {

    val imports = mutableMapOf<String, String>()
    val properties: Map<String, String> = parseProperties(source)

    init {
        for ((key, value) in properties) {
            if (key.startsWith("import.")) {
                val importId: String = key.removePrefix("import.")
                imports[importId] = value
            }
        }
    }

    override fun toString(): String = "ConfigFile(path='$path')"

}
