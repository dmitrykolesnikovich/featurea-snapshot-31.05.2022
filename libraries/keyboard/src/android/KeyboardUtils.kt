package featurea.keyboard

import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

fun KeyboardType?.toAndroidInputType(): Int {
    val keyboardType = this ?: return TYPE_CLASS_TEXT
    return when (keyboardType) {
        /*
        IMPORTANT `android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL` does not work for me, it starts blinking on
        switching between numeric and text keyboard in horizontal orientation
        */
        KeyboardType.NUMERIC -> TYPE_NUMBER_FLAG_DECIMAL
        else -> TYPE_CLASS_TEXT
    }
}
