package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import featurea.android.FeatureaFragment
import featurea.android.MainActivityProxy
import featurea.runtime.import

class AboutFragment : FeatureaFragment() {

    private val mainActivity by lazy { import(MainActivityProxy) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<AboutFragmentLayout>(inflater, R.layout.about_fragment, container, false).root
    }

    override fun onResume() {
        super.onResume()
        mainActivity.supportActionBar?.setTitle(R.string.about)
    }

}
