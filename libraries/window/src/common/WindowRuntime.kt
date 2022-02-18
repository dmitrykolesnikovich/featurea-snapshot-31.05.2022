package featurea.window

import featurea.runtime.*

expect fun WindowContainer(): ContainerBuilder

expect fun WindowModule(): ModuleBuilder

fun WindowRuntime(simulatorModule: Module, artifact: Dependency, setup: ModuleBlock): Runtime {
    val simulatorContainer: Container = simulatorModule.container
    return Runtime(simulatorContainer.registry) {
        exportComponents(artifact)
        appendContainer("featurea.window.DefaultWindowContainer") {
            include(simulatorContainer)
        }
        injectModule("featurea.window.DefaultWindowModule")
        init { appModule ->
            appModule.importComponent<Window>()
        }
        complete(setup)
    }
}
