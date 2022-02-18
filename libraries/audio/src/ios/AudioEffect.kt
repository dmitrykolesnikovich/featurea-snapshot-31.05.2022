package featurea.audio

import featurea.runtime.Module
import featurea.runtime.Component

actual class AudioEffect actual constructor(override val module: Module) : Component, AudioResource {

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

    actual fun pauseAll() {
        // no op
    }

    actual fun resumeAll() {
        // no op
    }

    actual fun stopAll() {
        // no op
    }

}
