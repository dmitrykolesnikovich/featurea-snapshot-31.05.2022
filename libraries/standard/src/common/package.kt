package featurea

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install
import featurea.window.WindowContainer
import featurea.window.WindowModule

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea") {
    includeExternals()

    "Application" to ::Application
    "Device" to ::Device
    "System" to System::class
    "WindowContainer" to ::WindowContainer
    "WindowModule" to ::WindowModule

    static {
        provideComponent(System())
    }
}

fun DependencyBuilder.ApplicationPlugin(plugin: Plugin<Application>) = install(plugin)
