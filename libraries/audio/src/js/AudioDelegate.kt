package featurea.audio

import featurea.System
import featurea.normalizedPath
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.create
import featurea.runtime.import
import org.w3c.dom.Audio as JsAudio

actual class AudioDelegate actual constructor(override val module: Module) : Component {

    private val system: System = import()

    private val cache = mutableMapOf<String, JsAudio>()

    actual fun newAudioTrack(filePath: String): AudioTrack {
        TODO()
    }

    actual fun newAudioEffect(filePath: String): AudioEffect {
        return create<AudioEffect>().apply { init(filePath) }
    }

    fun findJsAudioOrCreate(filePath: String): JsAudio {
        val existingJsAudio = cache[filePath]
        if (existingJsAudio != null) return existingJsAudio
        val audioSrc = "${system.workingDir ?: "bundle"}/$filePath".normalizedPath
        val newJsAudio = JsAudio(audioSrc)
        cache[filePath] = newJsAudio
        return newJsAudio
    }

    fun findJsAudioOrNull(filePath: String): JsAudio? {
        return cache[filePath]
    }

    fun removeJsAudio(filePath: String) {
        cache.remove(filePath)
    }

}
