package featurea.runtime

typealias Containers = ContainerProvider.() -> Unit

typealias Modules = ModuleProvider.() -> Unit

class Runtime internal constructor(val registry: ContainerRegistry) {

    private lateinit var components: Dependency
    lateinit var containerProvider: ContainerProvider
    lateinit var moduleProvider: ModuleProvider
    internal val initModuleBlocks = mutableListOf<ModuleBlock>()
    internal val completeModuleBlocks = mutableListOf<ModuleBlock>()
    internal val destroyModuleBlocks = mutableListOf<ModuleBlock>()

    fun exportComponents(components: Dependency) {
        this.components = components
    }

    fun init(block: ModuleBlock) {
        initModuleBlocks.add(block)
    }

    fun complete(block: ModuleBlock) {
        completeModuleBlocks.add(block)
    }

    fun destroy(block: ModuleBlock) {
        destroyModuleBlocks.add(block)
    }

    fun initContainer(container: Container) {
        containerProvider = ContainerProvider(container)
        containerProvider.initDependencyRegistry(container.dependencyRegistry)
    }

    fun injectContainer(canonicalName: String, init: Containers = {}) {
        initContainerProvider(ProvideType.INJECT, canonicalName, init)
    }

    fun injectDefaultContainer(init: Containers = {}) {
        initContainerProvider(ProvideType.INJECT, "featurea.runtime.DefaultContainer", init)
    }

    fun appendContainer(canonicalName: String, init: Containers = {}) {
        initContainerProvider(ProvideType.APPEND, canonicalName, init)
    }

    fun appendDefaultContainer(init: Containers = {}) {
        initContainerProvider(ProvideType.APPEND, "featurea.runtime.DefaultContainer", init)
    }

    fun replaceContainer(canonicalName: String, init: Containers = {}) {
        initContainerProvider(ProvideType.REPLACE, canonicalName, init)
    }

    fun injectModule(canonicalName: String, init: Modules = {}) {
        initModuleProvider(ProvideType.INJECT, canonicalName, init)
    }

    fun injectDefaultModule(init: Modules = {}) {
        initModuleProvider(ProvideType.INJECT, "featurea.runtime.DefaultModule", init)
    }

    fun appendModule(canonicalName: String, init: Modules = {}) {
        initModuleProvider(ProvideType.APPEND, canonicalName, init)
    }

    fun appendDefaultModule(init: Modules = {}) {
        initModuleProvider(ProvideType.APPEND, "featurea.runtime.DefaultModule", init)
    }

    fun replaceModule(canonicalName: String, init: Modules = {}) {
        initModuleProvider(ProvideType.REPLACE, canonicalName, init)
    }

    /*internals*/

    private fun initContainerProvider(type: ProvideType, canonicalName: String, init: Containers) {
        val dependencyRegistry: DependencyRegistry = DependencyRegistry.fromDependency(components)
        val constructor: ContainerConstructor? = dependencyRegistry.containers[canonicalName]
        val builder: ContainerBuilder = if (constructor != null) constructor() else DefaultContainer()
        containerProvider = ContainerProvider(registry, type, canonicalName, builder)
        containerProvider.initDependencyRegistry(dependencyRegistry)
        containerProvider.init()
    }

    private fun initModuleProvider(type: ProvideType, canonicalName: String, init: Modules) {
        val constructor: ModuleConstructor? = containerProvider.dependencyRegistry.modules[canonicalName]
        val builder: ModuleBuilder = if (constructor != null) constructor() else DefaultModule()
        moduleProvider = ModuleProvider(type, canonicalName, builder)
        moduleProvider.init()
    }

}
