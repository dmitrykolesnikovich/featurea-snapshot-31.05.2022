package featurea.android.simulator

import featurea.android.MainActivityProxy
import featurea.jvm.normalizedPath
import featurea.log
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.settings.SettingsService
import featurea.utils.currentThreadSpecifier
import featurea.utils.runOnApplicationThread
import java.io.File

internal var lastBundlePath: String? = null // quickfix todo improve

class Simulator(override val module: Module) : Component {

    private val mainActivity: SimulatorActivity by lazy { import(MainActivityProxy) as SimulatorActivity }
    private val simulator: Simulator = this
    private val settingsService: SettingsService = import()

    var withSplash: Boolean = false // quickfix todo improve
        private set

    fun openBundle(bundleFile: File, withSplash: Boolean) = runOnApplicationThread {
        if (!bundleFile.exists()) return@runOnApplicationThread
        log("[Simulator] openBundle: $bundleFile (${currentThreadSpecifier()})")
        simulator.withSplash = withSplash
        lastBundlePath = bundleFile.normalizedPath
        settingsService["lastBundlePath"] = bundleFile.normalizedPath
        if (withSplash) {
            mainActivity.updateActionBarPreference()
            mainActivity.navController.navigate(R.id.appFragment)
        } else {
            mainActivity.navController.navigate(R.id.action_bundlesFragment_to_appFragment)
        }
    }

}
