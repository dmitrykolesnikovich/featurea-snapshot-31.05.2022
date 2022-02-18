package featurea.android

import androidx.fragment.app.Fragment
import featurea.runtime.Module
import featurea.runtime.Component

open class FeatureaFragment : Fragment(), Component {
    override val module: Module by lazy { requireMainActivity().module }
}
