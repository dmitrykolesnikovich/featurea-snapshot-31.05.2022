package featurea.runtime

class ContainerProvider {

    internal lateinit var type: ProvideType
    internal lateinit var containerRegistry: ContainerRegistry
    internal lateinit var dependencyRegistry: DependencyRegistry
    internal lateinit var canonicalName: String
    internal var builder: ContainerBuilder = ContainerBuilder()
    internal var existingContainer: Container? = null
    val includes = mutableListOf<Container>()

    constructor(registry: ContainerRegistry, type: ProvideType, canonicalName: String, builder: ContainerBuilder) {
        this.type = type
        this.canonicalName = canonicalName
        this.builder = builder
        this.containerRegistry = registry
    }

    fun include(container: Container) {
        includes.add(container)
    }

    /*internals*/

    internal constructor(existingContainer: Container) {
        this.existingContainer = existingContainer
    }

    internal fun initDependencyRegistry(dependencyRegistry: DependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry
    }

    internal fun providerContainer(runtime: Runtime): Container {
        val existingContainer = existingContainer
        if (existingContainer != null) {
            return existingContainer
        }

        when (type) {
            ProvideType.INJECT -> {
                // existing
                @Suppress("NAME_SHADOWING")
                val existingContainer: Container? = containerRegistry.getOrNull(canonicalName)
                if (existingContainer != null) {
                    return existingContainer
                }

                // newly created
                val newContainer: Container = Container(runtime, dependencyRegistry)
                containerRegistry.injectContainer(canonicalName, newContainer)
                return newContainer
            }
            ProvideType.APPEND -> {
                val newContainer: Container = Container(runtime, dependencyRegistry)
                containerRegistry.appendContainer(canonicalName, newContainer)
                return newContainer
            }
            ProvideType.REPLACE -> {
                TODO()
            }
        }
    }

    internal fun initStaticBlocks(container: Container) {
        if (container.isStaticBlocksInitialized) return // quickfix todo avoid using boolean flag at all

        val staticBlocks: List<StaticBlock> = container.dependencyRegistry.artifact.staticBlocks
        /*container.provideComponent(container)*/
        for (staticBlock in staticBlocks) {
            container.staticBlock()
        }
        container.isStaticBlocksInitialized = true
    }

}
