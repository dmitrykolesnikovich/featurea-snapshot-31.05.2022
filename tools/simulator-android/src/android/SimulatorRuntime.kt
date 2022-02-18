package featurea.android.simulator

import android.Manifest
import androidx.core.app.ActivityCompat
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.ModuleBlock
import featurea.runtime.Runtime
import featurea.settings.SettingsService

fun SimulatorRuntime(setup: ModuleBlock = {}) = Runtime {
    exportComponents(artifact)
    injectContainer("featurea.android.simulator.SimulatorContainer")
    injectModule("featurea.android.simulator.SimulatorModule")
    complete(setup)
}

fun SimulatorContainer() = Container {
    await(MainActivityProxy::class)
    onInit { container ->
        val mainActivity: FeatureaActivity = container.import(MainActivityProxy)

        ActivityCompat.requestPermissions(mainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }
}

fun SimulatorModule() = Module {
    onInit { module ->
        module.importComponent<Simulator>()
        val settingsService: SettingsService = module.importComponent()

        settingsService["isFirstTimeCreated"] = true
        // >> quickfix todo improve
        if (settingsService[PREFERENCES_USERNAME, ""] == "") {
            settingsService[PREFERENCES_USERNAME] = "admin"
            settingsService[PREFERENCES_PASSWORD] = "12345"
        }
        // <<
    }
}
