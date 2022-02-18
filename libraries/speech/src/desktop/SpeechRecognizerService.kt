package featurea.speech

import featurea.runtime.Module
import featurea.runtime.Component

actual class SpeechRecognizerService actual constructor(override val module: Module) : Component {

    actual fun recognizeSpeech(language: String, response: (text: String) -> Unit) {
        response("light 16")
    }

}
