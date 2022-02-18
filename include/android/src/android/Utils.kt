package featurea.android

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.getInputMethodManager(): InputMethodManager {
    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}
