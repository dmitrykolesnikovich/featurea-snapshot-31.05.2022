package featurea.window

import featurea.runtime.*

expect fun WindowContainer(): ContainerBuilder

expect fun WindowModule(): ModuleBuilder

fun WindowRuntime(simulatorModule: Module, artifact: Dependency, setup: ModuleBlock): Runtime {
    val simulatorContainer: Container = simulatorModule.container
    return Runtime(simulatorContainer.registry) {
        exportComponents(artifact)
        appendDefaultContainer {
            include(simulatorContainer)
        }
        injectDefaultModule()
        init { appModule ->
            appModule.importComponent<Window>()
        }
        complete(setup)
    }
}
