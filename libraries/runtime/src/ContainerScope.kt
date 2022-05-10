package featurea.runtime

// >>
typealias StaticBlock = Container.() -> Unit
// typealias StaticBlock = ComponentContext.ContainerScope.() -> Unit
// <<

fun containerScope(artifact: Dependency, block: StaticBlock) {
    val runtime: Runtime = Runtime()
    runtime.exportComponents(artifact)
    runtime.injectDefaultContainer()
    val containerProvider: ContainerProvider = runtime.containerProvider
    val container: Container = containerProvider.providerContainer(runtime)
    containerProvider.initStaticBlocks(container)
    container.block()
}
