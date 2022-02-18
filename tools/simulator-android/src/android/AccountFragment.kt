package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import featurea.android.FeatureaFragment
import featurea.android.MainActivityProxy
import featurea.android.inflate
import featurea.android.sharedPreferences
import featurea.runtime.import

class AccountFragment : FeatureaFragment() {

    private val mainActivity by lazy { import(MainActivityProxy) as SimulatorActivity }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = inflater.inflate<AccountFragmentLayout>(R.layout.account_fragment, container)
        binding.logoutButton.setOnClickListener {
            mainActivity.sharedPreferences.edit {
                remove(PREFERENCES_USER_EXISTS)
            }
            mainActivity.navController.navigate(R.id.bundlesFragment)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mainActivity.supportActionBar?.setTitle(R.string.account)
    }

}
