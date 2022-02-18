package featurea.speech

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.speech") {
    include(featurea.content.artifact)

    "SpeechRecognizerService" to ::SpeechRecognizerService
}
