package featurea.content

import featurea.runtime.*

class ContentTypeRegistry(override val module: Module) : Component, ModuleRegistry<ContentType> {

    private val contentTypes = linkedMapOf<String, ContentType>()

    override fun registerComponent(canonicalName: String, component: ContentType) {
        val resourceCanonicalName: String = canonicalName.removeSuffix("ContentType")
        contentTypes[resourceCanonicalName] = component
    }

    fun parseOrNull(resourceTag: ResourceTag, key: String, value: String): List<String>? {
        for (contentType in contentTypes.values) {
            val parseQueue: List<String>? = contentType.parseOrNull(resourceTag, key, value)
            if (parseQueue != null) {
                return parseQueue
            }
        }
        return null
    }

    suspend fun loadResource(resource: Resource, loadingQueue: ArrayList<String>) {
        val contentType: ContentType = findContentType(resource.canonicalName)
        contentType.load(resource, loadingQueue)
    }

    suspend fun releaseResource(resource: Resource, releaseQueue: ArrayList<String>) {
        val contentType: ContentType = findContentType(resource.canonicalName)
        contentType.release(resource, releaseQueue)
    }

    /*internals*/

    private fun findContentType(canonicalName: String): ContentType {
        val contentType: ContentType? = contentTypes[canonicalName]
        if (contentType == null) {
            throw ContentTypeNotFoundException(canonicalName)
        }
        return contentType
    }

}

fun DependencyBuilder.contentTypes(plugin: Plugin<ContentTypeRegistry>) = install(plugin)
