package featurea.speech

import featurea.utils.log
import featurea.runtime.Module
import featurea.runtime.Component

actual class SpeechRecognizerService actual constructor(override val module: Module) : Component {

    actual fun recognizeSpeech(language: String, response: (text: String) -> Unit) {
        log("recognizeSpeech")
    }

}
