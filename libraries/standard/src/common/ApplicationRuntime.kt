package featurea

import featurea.runtime.*

expect fun ApplicationContainer(): ContainerBuilder

expect fun ApplicationModule(): ModuleBuilder

fun ApplicationRuntime(simulatorModule: Module, artifact: Dependency, setup: ModuleBlock): Runtime {
    val simulatorContainer: Container = simulatorModule.container
    return Runtime(simulatorContainer.registry) {
        exportComponents(artifact)
        appendDefaultContainer {
            include(simulatorContainer)
        }
        injectDefaultModule()
        init { appModule ->
            appModule.importComponent("featurea.window.Window")
        }
        complete(setup)
    }
}
