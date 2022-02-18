package featurea.audio

import featurea.runtime.Component
import featurea.runtime.Module

expect class AudioEffect(module: Module) : Component, AudioResource {
    fun play(isLoop: Boolean)
    fun stop()
    fun pause()
    fun resume()
    fun pauseAll()
    fun resumeAll()
    fun stopAll()
    fun adjustVolume(volume: Float)
}
