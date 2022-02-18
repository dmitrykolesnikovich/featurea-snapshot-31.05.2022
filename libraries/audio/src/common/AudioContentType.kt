package featurea.audio

import featurea.content.ContentType
import featurea.content.Resource
import featurea.content.ResourceTag
import featurea.content.isValidAudioResource
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

class AudioContentType(override val module: Module) : Component, ContentType {

    private val audio: Audio = import()

    override fun parseOrNull(resourceTag: ResourceTag, key: String, value: String): List<String>? {
        if (!value.isValidAudioResource()) return null

        val audioType = if (key.endsWith("Sound")) "sound" else "music"
        return listOf("${audioType}:/${value}")
    }

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        loadingQueue.add(resource.path)
        /*if (isInstrumentationEnable) return*/

        val filePath = resource.files[0]
        val audioType: String = resource.manifest["audioType"] ?: error("resource: $resource")
        val audioResource: AudioResource = when (audioType) {
            "sound" -> audio.findSoundOrCreate(filePath)
            "music" -> audio.findMusicOrCreate(filePath)
            else -> error("audioType: $audioType")
        }
        audioResource.load()
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        releaseQueue.add(resource.path)
        /*if (isInstrumentationEnable) return*/

        val audioType: String = resource.manifest["audioType"] ?: error("resource: $resource")
        val audioResource: AudioResource? = when (audioType) {
            "sound" -> audio.findSoundOrNull(resource.path)
            "music" -> audio.findMusicOrNull(resource.path)
            else -> error("audioType: $audioType")
        }
        audioResource?.release()
    }

}
