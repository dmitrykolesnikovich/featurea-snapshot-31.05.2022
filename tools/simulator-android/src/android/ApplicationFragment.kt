package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import featurea.Application
import featurea.android.*
import featurea.nameWithoutExtension
import featurea.runtime.Module
import featurea.runtime.Provide
import featurea.runtime.import
import featurea.window.MainActivityContentView

@Provide(MainActivityContentViewProxy::class)
@Provide(ProgressBarProxy::class)
@Provide(ProgressLayoutProxy::class)
@Provide(ProgressTextViewProxy::class)
class ApplicationFragment : FeatureaFragment() {

    val app: Application by lazy { appModule.importComponent() }
    val appFragment: ApplicationFragment = this
    val mainActivity: SimulatorActivity by lazy { import(MainActivityProxy) as SimulatorActivity }
    val simulator: Simulator by lazy { import() }

    private lateinit var appModule: Module
    var mainActivityContentView: MainActivityContentView? = null

    // https://stackoverflow.com/a/23519289/909169
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivityContentView = mainActivityContentView
        if (mainActivityContentView == null) {
            val bundlePath = lastBundlePath
            if (bundlePath != null) {
                mainActivity.launchBundle(bundlePath) { appModule, contentView ->
                    appFragment.appModule = appModule
                    appFragment.mainActivityContentView = contentView
                    appModule.provideComponent(MainActivityContentViewProxy(contentView))
                    appModule.installLoaderLayout(inflater, simulator.withSplash)
                }
            }
        } else {
            val parentView = mainActivityContentView.parent as ViewGroup
            parentView.removeView(mainActivityContentView)
        }
        return appFragment.mainActivityContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        mainActivity.supportActionBar?.title = lastBundlePath?.nameWithoutExtension
    }

    override fun onPause() {
        super.onPause()
        app.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        app.onStop()
        app.onDestroy()
    }

}
