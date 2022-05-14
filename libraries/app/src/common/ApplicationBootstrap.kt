package featurea.app

import featurea.runtime.Action
import featurea.runtime.Dependency
import featurea.runtime.Runtime
import featurea.utils.Bootstrap

fun Bootstrap.setupApplication(complete: () -> Unit): Bootstrap {
    check(!isSetup)
    // todo setup application
    return Bootstrap
}

fun bootstrapApplication(export: Dependency, setup: Action = {}): Runtime = Runtime {
    exportComponents(export)
    injectContainer("featurea.app.ApplicationContainer")
    injectModule("featurea.app.ApplicationModule")
    complete(setup)
}
