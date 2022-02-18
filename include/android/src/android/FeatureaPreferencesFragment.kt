package featurea.android

import android.os.Handler
import androidx.preference.PreferenceFragmentCompat
import featurea.runtime.Module
import featurea.runtime.Component

abstract class FeatureaPreferencesFragment : PreferenceFragmentCompat(), Component {
    override val module: Module by lazy { requireMainActivity().module }
}
