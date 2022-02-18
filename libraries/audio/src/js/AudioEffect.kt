package featurea.audio

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

actual class AudioEffect actual constructor(override val module: Module) : Component, AudioResource {

    private val delegate: AudioDelegate = import()

    lateinit var filePath: String

    override fun init(filePath: String) {
        this.filePath = filePath
    }

    override fun load() {
        val jsAudio = delegate.findJsAudioOrCreate(filePath)
        jsAudio.load()
    }

    override fun release() {
        delegate.removeJsAudio(filePath)
    }

    actual fun play(isLoop: Boolean) {
        val jsAudio = delegate.findJsAudioOrNull(filePath) ?: error("Audio not found: $filePath")
        jsAudio.play()
    }

    actual fun stop() {
        val jsAudio = delegate.findJsAudioOrNull(filePath) ?: error("Audio not found: $filePath")
        jsAudio.currentTime = 0.0
        jsAudio.pause()
        jsAudio.currentTime = 0.0
    }

    actual fun pause() {
        val jsAudio = delegate.findJsAudioOrNull(filePath) ?: error("Audio not found: $filePath")
        jsAudio.pause()
    }

    actual fun resume() {
        val jsAudio = delegate.findJsAudioOrNull(filePath) ?: error("Audio not found: $filePath")
        jsAudio.play()
    }

    actual fun pauseAll() {
        pause()
    }

    actual fun resumeAll() {
        resume()
    }

    actual fun stopAll() {
        stop()
    }

    actual fun adjustVolume(volume: Float) {
        val jsAudio = delegate.findJsAudioOrNull(filePath) ?: error("Audio not found: $filePath")
        jsAudio.volume = volume.toDouble()
    }

}