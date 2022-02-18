package featurea.android

import android.app.Dialog
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

abstract class FeatureaPreferenceDialogFragmentCompat<T : FeatureaDialogPreference> : PreferenceDialogFragmentCompat() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        preference.module = requireMainActivity().module
        return super.onCreateDialog(savedInstanceState)
    }

    override fun getPreference(): T = super.getPreference() as T

}