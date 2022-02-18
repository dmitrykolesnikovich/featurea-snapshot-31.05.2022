@file:JvmName("LoggerUtils")

package featurea

import android.util.Log

private const val TAG: String = "FEATUREA"

actual fun log(message: Any?, isFailure: Boolean) {
    if (message is Throwable) {
        message.printStackTrace()
    } else {
        Log.d(TAG, message.toString())
    }
}
