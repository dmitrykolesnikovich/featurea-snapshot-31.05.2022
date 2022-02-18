package featurea.content

class ContentTypeNotFoundException(canonicalName: String) : RuntimeException(canonicalName)

interface ContentType {
    // todo refactor to `parse(resourceTag: ResourceTag, key: String, value: String, parseQueue: ArrayList<String>)`
    fun parseOrNull(resourceTag: ResourceTag, key: String, value: String): List<String>? = null
    suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {}
    suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {}
}
