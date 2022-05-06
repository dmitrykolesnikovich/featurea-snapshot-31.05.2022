package featurea.window

import featurea.Application
import featurea.ApplicationDelegate
import featurea.bootstrapApplication
import featurea.runtime.*

// https://www.youtube.com/watch?v=EuRy4L4WixU
fun bootstrap(delegate: Component.() -> ApplicationDelegate, includes: DependencyBuilder.() -> Unit) = Runtime {
    exportComponents(DefaultArtifact(includes))
    injectContainer("featurea.window.WindowContainer")
    injectModule("featurea.window.WindowModule")
    complete { appModule ->
        val app: Application = appModule.importComponent()
        app.delegate = app.delegate()
    }
}
