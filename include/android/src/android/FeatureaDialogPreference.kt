package featurea.android

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import featurea.runtime.Module
import featurea.runtime.Component

open class FeatureaDialogPreference(context: Context, attrs: AttributeSet) : Component, DialogPreference(context, attrs) {
    override lateinit var module: Module
}
