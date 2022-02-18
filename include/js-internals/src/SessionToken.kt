package featurea.js

import kotlinx.browser.window as jsWindow

// used by `platform/libs/webSocket/source/js/WebSocket.kt`
class SessionToken(private val expirationTimeout: Int) {

    lateinit var onExpire: () -> Unit

    var isValid: Boolean = false
        private set
    private var validateTime: Double = -1.0
    private var intervalId: Int = -1

    // onConnect, onSuccess
    fun validate() {
        if (!isValid) {
            isValid = true
            intervalId = jsWindow.setInterval({
                val now = getTimeMillis()
                if (validateTime == -1.0) {
                    validateTime = now
                }
                if (now - validateTime > expirationTimeout) {
                    invalidate()
                    onExpire()
                }
            }, expirationTimeout)
        }

        validateTime = getTimeMillis()
    }

    // onDisconnect, onFailure
    fun invalidate() {
        jsWindow.clearInterval(intervalId)

        isValid = false
        validateTime = -1.0
        intervalId = -1
    }

}

private fun getTimeMillis(): Double = js("Date.now()").unsafeCast<Double>() // quickfix todo use standard library
