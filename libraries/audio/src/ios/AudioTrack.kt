package featurea.audio

import featurea.runtime.Component
import featurea.runtime.Module

actual class AudioTrack actual constructor(override val module: Module) : Component, AudioResource {

    override fun init(filePath: String) {
        // no op
    }

    override fun load() {
        // no op
    }

    override fun release() {
        // no op
    }

    actual fun play(isLoop: Boolean) {
        // no op
    }

    actual fun pause() {
        // no op
    }

    actual fun stop() {
        // no op
    }

    actual fun resume() {
        // no op
    }

    actual fun adjustVolume(volume: Float) {
        // no op
    }

}
