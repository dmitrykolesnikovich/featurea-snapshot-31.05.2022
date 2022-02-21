package featurea.android.simulator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import featurea.android.hideKeyboard
import featurea.android.requireMainActivity
import featurea.android.snackbar
import featurea.android.simulator.databinding.FragmentLoginBinding
import featurea.runtime.import
import featurea.settings.SettingsService

class LoginFragment : Fragment() {

    private val settingsService by lazy { requireMainActivity().import<SettingsService>() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireMainActivity().supportActionBar?.hide()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = DataBindingUtil.inflate<FragmentLoginBinding>(inflater, R.layout.fragment_login, container, false)
        layout.loginButton.setOnClickListener {
            val username = layout.usernameTextView.text.toString()
            val password = layout.passwordTextView.text.toString()
            if (settingsService[PREFERENCES_USERNAME, ""] == username && settingsService[PREFERENCES_PASSWORD, ""] == password) {
                requireActivity().hideKeyboard()
                requireMainActivity().supportActionBar?.show()
                settingsService[PREFERENCES_USER_EXISTS] = true
                findNavController().popBackStack()
            } else {
                requireActivity().snackbar("Wrong login or password")
            }
        }
        return layout.root
    }

}
