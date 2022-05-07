package featurea.text

import featurea.System
import featurea.content.ResourceNotFoundException
import featurea.utils.readTextOrNull
import featurea.runtime.Component
import featurea.runtime.import

class TextContent(val system: System) {

    private val existingTexts = mutableMapOf<String, String>()

    suspend fun findText(filePath: String, limit: Int = -1): String {
        return findTextOrNull(filePath, limit) ?: throw ResourceNotFoundException(filePath)
    }

    suspend fun findTextOrNull(filePath: String, limit: Int = -1): String? {
        // 1. existing
        val existingText: String? = existingTexts[filePath]
        if (existingText != null) {
            return existingText
        }

        // 2. newly created
        val text: String? = system.readTextOrNull(filePath, limit)
        if (text != null) {
            existingTexts[filePath] = text
        }
        return text
    }

    fun removeCachedText(filePath: String) {
        existingTexts.remove(filePath)
    }

    fun clearCache() {
        existingTexts.clear()
    }

}

suspend fun Component.readText(filePath: String): String {
    val system: System = import()
    return checkNotNull(system.readTextOrNull(filePath))
}
