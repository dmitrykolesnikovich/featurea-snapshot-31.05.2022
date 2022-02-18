package featurea.speech

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import featurea.android.FeatureaActivityListener
import featurea.android.MainActivityProxy
import featurea.log
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import

private const val REQ_CODE_SPEECH_INPUT = 100

actual class SpeechRecognizerService actual constructor(override val module: Module) : Component {

    private val mainActivity = import(MainActivityProxy)
    private lateinit var response: (text: String) -> Unit

    init {
        mainActivity.listeners.add(object : FeatureaActivityListener {
            override suspend fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
                    val extraResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (extraResults != null && extraResults.isNotEmpty()) {
                        val text = extraResults[0]
                        response(text)
                    }
                }
            }
        })
    }

    actual fun recognizeSpeech(language: String, response: (text: String) -> Unit) {
        this.response = response
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        try {
            mainActivity.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            log(e)
        }
    }

}
