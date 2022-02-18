package featurea.android.simulator

import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import featurea.Application
import featurea.StopApplicationListener
import featurea.System
import featurea.android.*
import featurea.loader.Loader
import featurea.runtime.*
import featurea.window.MainActivityContentView
import featurea.window.WindowRuntime
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class BundleLauncher(override val module: Module) : Component {

    private val app: Application = import()
    private val mainActivity: FeatureaActivity = import(MainActivityProxy)
    private val mainSurfaceView: GLSurfaceView = import(MainSurfaceViewProxy)
    private val progressBar: ProgressBar = import(ProgressBarProxy)
    private val progressLayout: ViewGroup = import(ProgressLayoutProxy)
    private val progressTextView: TextView = import(ProgressTextViewProxy)
    private val simulator: Simulator = import()

    private var isLoading: Boolean = false
    var isDone: Boolean = false

    init {
        app.repeatOnBuildApplication {
            buildApplication()
        }
        if (simulator.withSplash) {
            // >> quickfix todo improve

            // 1)
            /*
            var isComplete = false

            var isLoad = false
            repeatTaskOnStartLoadBundle {
                progressLayoutProxy.onComplete = {
                    isComplete = true
                    if (isLoad) {
                        finishLoadingWithSplash()
                    }
                }
            }
            repeatTaskOnFinishLoadBundleScreen {
                isLoad = true
                if (isComplete) {
                    finishLoadingWithSplash()
                }
            }
            */

            // 2)

            // <<
            mainActivity.runOnUiThread {
                mainSurfaceView.background = null
                mainActivity.updateActionBarPreference()
                // progressLayout.visibility = GONE // quickfix todo improve
            }
        } else {
            app.repeatOnStartLoading {
                startLoading()
            }
            app.listeners.add(StopApplicationListener {
                stopApplication()
            })
        }
    }

    /*internals*/

    private fun startLoading() {
        isLoading = true
        mainActivity.runOnUiThread {
            mainActivity.window.setBackgroundDrawable(Color.WHITE.toDrawable())
            progressLayout.startAnimation(fadeInAnimation(time = 200) {
                progressLayout.isVisible = true
            })
            mainSurfaceView.background = Color.WHITE.toDrawable()
        }
    }

    private fun completeLoading() {
        mainActivity.runOnUiThread {
            progressLayout.isVisible = false
        }
        postDelayed(100) {
            isDone = true
        }
    }

    private fun buildApplication() {
        isLoading = false
        mainActivity.runOnUiThread {
            mainSurfaceView.background = null
            mainActivity.updateActionBarPreference()
            progressBar.isVisible = false
            progressTextView.isVisible = false
            postDelayed(400) {
                progressLayout.startAnimation(fadeOutAnimation(time = 200) {
                    progressLayout.isVisible = false
                })
            }
            postDelayed(500) {
                isDone = true
            }
        }
    }

    private fun stopApplication() {
        if (!isLoading) {
            mainSurfaceView.background = Color.BLACK.toDrawable()
        }
    }

}

@Provide(MainActivityProxy::class)
fun SimulatorActivity.launchBundle(bundlePath: String, init: (appModule: Module, mainActivityContentView: MainActivityContentView) -> Unit) {
    val mainActivity: SimulatorActivity = this
    proxyScope {
        initContainer {
            provide(MainActivityProxy(mainActivity))
        }
        initModule { appModule ->
            val mainActivityContentView = appModule.createComponent<MainActivityContentView> {
                layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                background = Color.BLACK.toDrawable()
            }
            val system: System = appModule.importComponent()

            system.workingDir = bundlePath
            init(appModule, mainActivityContentView)
        }
        WindowRuntime(simulatorModule = module, artifact) { appModule ->
            val loader: Loader = appModule.importComponent()
            appModule.importComponent<BundleLauncher>() // IMPORTANT `BundleLauncher` is service not feature
            appModule.updateOrientationPreference() // quickfix todo find better place
            runBlocking {
                loader.loadBundle(bundlePath)
            }
        }
    }
}
