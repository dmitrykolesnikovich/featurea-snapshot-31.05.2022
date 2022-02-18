package featurea.audio

import featurea.runtime.Component
import featurea.runtime.Module

expect class AudioDelegate(module: Module) : Component {
    fun newAudioTrack(filePath: String): AudioTrack
    fun newAudioEffect(filePath: String): AudioEffect
}
