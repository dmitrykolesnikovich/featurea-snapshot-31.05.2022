package featurea.audio

import featurea.runtime.Component
import featurea.runtime.Module

expect class AudioTrack(module: Module) : Component, AudioResource {
    fun play(isLoop: Boolean)
    fun pause()
    fun stop()
    fun resume()
    fun adjustVolume(volume: Float)
}
