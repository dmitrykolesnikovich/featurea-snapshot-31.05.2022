package featurea.speech

import featurea.runtime.Component
import featurea.runtime.Module

expect class SpeechRecognizerService(module: Module) : Component {
    fun recognizeSpeech(language: String, response: (text: String) -> Unit)
}
