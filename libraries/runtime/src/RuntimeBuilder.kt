package featurea.runtime

import featurea.runtime.RuntimeState.*
import kotlin.reflect.KClass

class RuntimeBuilder(val receiver: Any, val init: RuntimeBuilder.() -> Runtime) : ProxyScope {

    private var state: RuntimeState = RUNTIME_NEW

    private lateinit var runtime: Runtime
    private lateinit var container: Container
    private lateinit var module: Module

    private val containerBuilder by lazy { runtime.containerProvider.builder }
    private val moduleBuilder by lazy { runtime.moduleProvider.builder }
    private val awaitProxiesInContainer by lazy { containerBuilder.awaitProxies.toMutableList() }
    private val awaitProxiesInModule by lazy { ArrayList(moduleBuilder.awaitProxies) }

    private val initContainerBlocks = mutableListOf<() -> Unit>()
    private val initModuleBlocks = mutableListOf<ModuleBlock>()
    private val destroyModuleBlocks = mutableListOf<ModuleBlock>()
    private lateinit var onSuccess: () -> Unit
    private lateinit var onFailure: () -> Unit

    fun onInitContainer(block: () -> Unit) {
        initContainerBlocks.add(block)
    }

    fun onInitModule(block: ModuleBlock) {
        initModuleBlocks.add(block)
    }

    fun onDestroyModule(block: ModuleBlock) {
        destroyModuleBlocks.add(block)
    }

    /*internals*/

    internal fun build(complete: () -> Unit) {
        onSuccess = complete
        onFailure = complete
        try {
            // 1. RUNTIME_INIT_START
            state = RUNTIME_INIT_START
            runtime = init()
            runtime.destroyModuleBlocks.addAll(destroyModuleBlocks) // quickfix todo improve
            container = runtime.containerProvider.providerContainer(runtime)
            module = runtime.moduleProvider.provideModule(runtime, container)

            // 2. CONTAINER_INIT_START
            state = CONTAINER_INIT_START
            for (initContainerBlock in initContainerBlocks) {
                initContainerBlock()
            }
            runtime.containerProvider.initStaticBlocks(container)

            // 3. CONTAINER_INIT_COMPLETE
            state = CONTAINER_INIT_COMPLETE
            containerBuilder.onInitBlock?.invoke(container)

            // 4. CONTAINER_INIT_COMPLETE
            if (state == CONTAINER_INIT_COMPLETE) {
                createContainer()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure()
        }
    }

    private fun createContainer() {
        try {
            if (awaitProxiesInContainer.isEmpty()) {
                // 5. CONTAINER_CREATE
                state = CONTAINER_CREATE
                containerBuilder.onCreateBlock?.invoke(container)

                // 6. MODULE_INIT
                state = MODULE_INIT
                if (!module.isInitComplete) {
                    for (component in container.components) {
                        module.installPlugin(plugin = component::class)
                    }
                    for (initModuleBlock in initModuleBlocks) {
                        initModuleBlock(module)
                    }
                    for (initModuleBlock in runtime.initModuleBlocks) {
                        initModuleBlock(module)
                    }
                    moduleBuilder.initBlock?.invoke(moduleBuilder.InitBlock(), module)
                    module.installRegistries()
                }
                createModule()
            } else {
                // 5'. CONTAINER_CREATE_AWAIT
                state = CONTAINER_CREATE_AWAIT
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure()
        }
    }

    private fun createModule() {
        try {
            if (awaitProxiesInModule.isEmpty()) {
                // 7. MODULE_CREATE
                state = MODULE_CREATE
                if (!module.isInitComplete) {
                    moduleBuilder.createBlock?.invoke(module)
                }

                // 8. MODULE_BUILD
                state = MODULE_BUILD
                for (completeModuleBlock in runtime.completeModuleBlocks) {
                    completeModuleBlock(module)
                }

                // 9. RUNTIME_INIT_COMPLETE
                state = RUNTIME_INIT_COMPLETE
                module.isInitComplete = true
                onSuccess()
            } else {
                // 7'. MODULE_CREATE_AWAIT
                state = MODULE_CREATE_AWAIT
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure()
        }
    }

    override fun provide(proxy: Proxy<*>) {
        try {
            val type: KClass<out Proxy<*>> = proxy::class
            if (awaitProxiesInContainer.contains(type)) {
                awaitProxiesInContainer.remove(type)
                container.provideComponent(proxy)
            } else {
                awaitProxiesInModule.remove(type)
                module.provideComponent(proxy)
            }
            when (state) {
                CONTAINER_INIT_COMPLETE -> createContainer() // 5'. CONTAINER_INIT_COMPLETE
                CONTAINER_CREATE_AWAIT -> createContainer() //
                MODULE_CREATE_AWAIT -> createModule() // 7'. MODULE_CREATE_AWAIT
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure()
        }
    }

}

// constructor
fun Runtime(registry: ContainerRegistry = ContainerRegistry(), init: Runtime.() -> Unit = {}): Runtime =
    Runtime(registry).apply(init)

// constructor
fun DefaultRuntime(artifact: Dependency = DefaultArtifact(), setup: Action = {}): Runtime = Runtime {
    exportComponents(artifact)
    injectContainer("featurea.runtime.DefaultContainer")
    injectModule("featurea.runtime.DefaultModule")
    complete(setup)
}
