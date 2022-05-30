package featurea.studio.home.components

import featurea.config.Config
import featurea.content.ResourceTag
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.utils.toSimpleName

class DefaultsService(override val module: Module) : Component {

    private val defaults = Config("defaults")
    private var counter: Int = 0

    fun createDefaultRmlTag(canonicalName: String, parent: ResourceTag? = null): ResourceTag {
        counter++
        val simpleName: String = canonicalName.toSimpleName()
        val rmlTag = when {
            parent != null -> ResourceTag(simpleName, parent)
            else -> ResourceTag(simpleName)
        }
        val defaultProperties: Map<String, String> = defaults.propertiesOf(canonicalName)
        for ((key, script) in defaultProperties) {
            val value = script
                .replaceCounter(counter)        // quickfix todo conceptualize
                .replaceSimpleName(simpleName)  // quickfix todo conceptualize
            rmlTag.attributes[key] = value
        }
        return rmlTag
    }

}

private fun String.replaceCounter(counter: Int): String = replace("\\$\\{counter\\}".toRegex(), "$counter")
private fun String.replaceSimpleName(simpleName: String): String = replace("\\$\\{simpleName\\}".toRegex(), simpleName)
