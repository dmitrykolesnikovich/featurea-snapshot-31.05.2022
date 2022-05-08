package featurea.app

import featurea.runtime.Action
import featurea.runtime.Dependency
import featurea.runtime.Runtime

fun bootstrapApplication(export: Dependency, setup: Action = {}): Runtime = Runtime {
    exportComponents(export)
    injectContainer("featurea.app.ApplicationContainer")
    injectModule("featurea.app.ApplicationModule")
    complete(setup)
}
