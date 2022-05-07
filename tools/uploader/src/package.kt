package featurea.deviceChooser.studio

import featurea.desktop.Preferences
import featurea.desktop.PreferencesDelegate
import featurea.utils.firstStringOrNull
import featurea.runtime.*
import featurea.script.Script

val artifact = Artifact("featurea.deviceChooser.studio") {
    "DevicesChooserDialog" to ::DevicesChooserDialog
    "Docket" to ::Docket
}

/*actions*/

class Docket(override val module: Module) : Component, Script {

    private val deviceChooser: DevicesChooserDialog = import()

    override suspend fun executeAction(action: String, args: List<Any?>, isSuper: Boolean): Any {
        val value: String? = args.firstStringOrNull()
        when (action) {
            "DevicesChooserDialog.show" -> deviceChooser.show(value ?: studioPreferences.deviceChooserBundle)
        }
        return Unit
    }

}

/*preferences*/

val studioPreferences: Preferences = Preferences("studio")

var Preferences.deviceChooserBundle: String by PreferencesDelegate("deviceChooserBundle")
