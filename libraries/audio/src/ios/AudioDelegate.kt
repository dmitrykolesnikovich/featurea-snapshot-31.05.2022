package featurea.audio

import featurea.runtime.Component
import featurea.runtime.Module

actual class AudioDelegate actual constructor(override val module: Module) : Component {
    actual fun newAudioTrack(filePath: String): AudioTrack = AudioTrack(module)
    actual fun newAudioEffect(filePath: String): AudioEffect = AudioEffect(module)
}
