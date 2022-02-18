package featurea.audio.reader

import featurea.Bundle
import featurea.Properties
import featurea.content.Resource
import featurea.content.ResourceReader
import featurea.content.isValidAudioResource

class AudioReader : ResourceReader {

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (!resourcePath.isValidAudioResource()) return null

        val audioType: String = when {
            resourcePath.startsWith("sound") -> "sound"
            resourcePath.startsWith("music") -> "music"
            else -> "sound" // quickfix todo revert to `error("resourcePath: $resourcePath")`
        }
        val filePath = resourcePath.removePrefix("sound:/") // quickfix todo improve
        return Resource(filePath) {
            Properties("audioType" to audioType)
        }
    }

}
