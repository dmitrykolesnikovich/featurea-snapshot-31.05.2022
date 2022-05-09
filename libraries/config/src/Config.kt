package featurea.config

import featurea.content.ResourceSchema
import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.toSimpleName
import featurea.utils.runBlocking

class Config(val name: String) {

    val files = mutableListOf<ConfigFile>()
    val properties = linkedMapOf<String, String>()
    internal var rmlSchema: ResourceSchema? = null

    fun initProperties() {
        for (file in files.asReversed()) {
            properties.putAll(file.properties)
        }
    }

    operator fun get(key: String?, defaultValue: String): String {
        return get(key) ?: defaultValue
    }

    operator fun get(key: String?): String? {
        val rmlSchema: ResourceSchema = checkNotNull(rmlSchema)
        if (key == null) {
            return null
        }
        val value: String? = findValueOrNull(key)
        if (value != null) {
            return value
        }
        val superKey = rmlSchema.findSuperKeyForKeyOrNull(key)
        return get(superKey) // IMPORTANT recursion by design
    }

    fun propertiesOf(canonicalName: String): Map<String, String> {
        val rmlSchema: ResourceSchema = checkNotNull(rmlSchema)
        val result = mutableMapOf<String, String>()
        var currentCanonicalName: String = canonicalName
        while (currentCanonicalName != "kotlin.Any") {
            val simpleName: String = currentCanonicalName.toSimpleName()
            for ((key, value) in properties) {
                if (key.startsWith("${simpleName}.")) {
                    if (result[key.toSimpleName()] == null) {
                        result[key.toSimpleName()] = value
                    }
                }
            }
            currentCanonicalName = rmlSchema.superCanonicalClassNameByKey[simpleName] ?: "kotlin.Any"
        }
        return result
    }

    fun exists(): Boolean {
        return files.isNotEmpty()
    }

    /*internals*/

    private fun findValueOrNull(key: String): String? {
        for (file in files) {
            val value: String? = file.properties[key]
            if (value != null) {
                return value
            }
        }
        return null
    }

}

/*convenience*/

fun Component.Config(name: String): Config = runBlocking {
    val configContent: ConfigContent = import()
    configContent.findConfig(name)
}

fun Module.importConfig(name: String): Config = runBlocking {
    val configContent: ConfigContent = importComponent()
    configContent.findConfig(name)
}

fun Container.importConfig(name: String): Config = runBlocking {
    val configContent: ConfigContent = import()
    configContent.findConfig(name)
}
