package featurea.android

import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import featurea.runtime.Component
import featurea.runtime.Module
import kotlinx.coroutines.runBlocking

open class FeatureaActivity : AppCompatActivity(), Component {

    override lateinit var module: Module
    val listeners = mutableListOf<FeatureaActivityListener>()
    val handler = Handler()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runBlocking {
            for (listener in listeners) {
                listener.onConfigurationChanged(newConfig)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        runBlocking {
            for (listener in listeners) {
                listener.onNewIntent(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        runBlocking {
            for (listener in listeners) {
                listener.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

}

fun Fragment.requireMainActivity(): FeatureaActivity {
    return requireActivity() as FeatureaActivity
}
