package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import featurea.android.FeatureaFragment
import java.io.File
import featurea.android.inflate

class ViewLogFragment : FeatureaFragment() {

    private lateinit var binding: FragmentViewLogLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = inflater.inflate<FragmentViewLogLayout>(R.layout.fragment_view_log, container)
        val filePath = arguments?.getString("filePath") ?: error("filePath not found")
        binding.logTextView.text = File(filePath).readText()
        return binding.root
    }

}
