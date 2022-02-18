package featurea.settings

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.settings") {
    "SettingsService" to ::SettingsService
}
