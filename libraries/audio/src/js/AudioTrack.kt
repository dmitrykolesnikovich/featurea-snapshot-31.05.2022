package featurea.audio

import featurea.runtime.Module
import featurea.runtime.Component

actual class AudioTrack actual constructor(override val module: Module) : Component, AudioResource {
    override fun init(filePath: String): Unit = TODO()
    override fun load(): Unit = TODO()
    override fun release(): Unit = TODO()
    actual fun play(isLoop: Boolean): Unit = TODO()
    actual fun pause(): Unit = TODO()
    actual fun stop(): Unit = TODO()
    actual fun resume(): Unit = TODO()
    actual fun adjustVolume(volume: Float): Unit = TODO()
}
