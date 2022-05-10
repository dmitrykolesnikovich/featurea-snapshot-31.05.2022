package featurea.runtime

import kotlin.reflect.KClass

typealias ContainerBlock<T> = (Container) -> T

class Container(val runtime: Runtime, val dependencyRegistry: DependencyRegistry) {

    lateinit var registry: ContainerRegistry
    lateinit var key: String
    internal var isStaticBlocksInitialized: Boolean = false
    val staticModule: Module = Module(runtime, this)
    val modules = ComponentRegistry<Module>()
    val components = ComponentRegistry<Any>()
    val componentListeners = mutableListOf<ComponentListener>()

    fun injectModule(canonicalName: String, module: Module) {
        check(!modules.contains(module))
        check(!modules.containsKey(canonicalName))
        modules.inject(canonicalName, module)
    }

    fun appendModule(canonicalName: String, module: Module) {
        check(!modules.contains(module))
        modules.append(canonicalName, module)
    }

    fun removeModule(module: Module) {
        removeModule(module.key)
    }

    fun removeModule(key: String) {
        val module: Module? = modules.remove(key)
        if (module != null) {
            module.onDestroy()
        }
    }

    fun provideComponent(component: Any) {
        if (component is ComponentListener) componentListeners.add(component) // quickfix todo find better place
        val type: KClass<out Any> = component::class
        val canonicalName: String = dependencyRegistry.findCanonicalName(type)
        components.inject(canonicalName, component)
        for (componentListener in componentListeners) {
            componentListener.provideComponent(canonicalName, component)
        }
        installPlugin(plugin = type)
    }

    inline fun <reified T : Any> findStaticOrNull(): T? {
        val canonicalName: String = dependencyRegistry.findCanonicalName(T::class)
        return findStaticOrNull(canonicalName)
    }

    fun <T : Any> findStaticOrNull(canonicalName: String): T? {
        val existingComponent: T? = components.getOrNull(canonicalName)
        if (existingComponent != null) {
            return existingComponent
        }

        for (include in runtime.containerProvider.includes) {
            val component: T? = include.findStaticOrNull(canonicalName)
            if (component != null) {
                return component
            }
        }

        return null
    }

    inline fun <reified T : Any> import(): T {
        val staticComponent: T? = findStaticOrNull()
        if (staticComponent == null) {
            throw DependencyNotFoundException(T::class)
        }
        return staticComponent
    }

    fun <T : Any> import(delegate: Delegate<T>): T {
        val proxyType: KClass<out Proxy<T>> = delegate.proxyType
        val canonicalName: String = dependencyRegistry.findCanonicalName(proxyType)
        val staticComponent: Proxy<T>? = findStaticOrNull(canonicalName)
        if (staticComponent == null) {
            throw DependencyNotFoundException(proxyType)
        }
        return staticComponent.delegate
    }

    fun findModule(canonicalName: String): Module {
        val module: Module? = findModuleOrNull(canonicalName)
        if (module == null) {
            throw ModuleNotFoundException(canonicalName)
        }
        return module
    }

    fun findModuleOrNull(canonicalName: String): Module? {
        val existingModule: Module? = modules.getOrNull(canonicalName)
        if (existingModule != null) {
            return existingModule
        }

        for (includedContainer in runtime.containerProvider.includes) {
            val includedModule: Module? = includedContainer.findModuleOrNull(canonicalName)
            if (includedModule != null) {
                return includedModule
            }
        }

        return null
    }

    fun destroy() {
        registry.removeContainer(this)
    }

    /*internals*/

    internal fun onDestroy() {
        runtime.containerProvider.builder.onDestroyBlock?.invoke(this)
        for (module in modules) {
            module.destroy()
        }
    }

    private fun installPlugin(plugin: KClass<*>) {
        val features: List<String>? = dependencyRegistry.features[plugin]
        if (features != null) {
            for (feature in features) {
                val containerComponentConstructor = dependencyRegistry.containerComponents[feature]
                if (containerComponentConstructor != null) {
                    val containerComponent = containerComponentConstructor(this)
                    components.inject(feature, containerComponent)
                }
            }
        }
    }

}

// constructor
fun Container(init: ContainerBuilder.() -> Unit = {}): ContainerBuilder = ContainerBuilder().apply(init)

// constructor
fun DefaultContainer() = Container()

/*convenience*/

val Container.artifact: Dependency get() = dependencyRegistry.artifact
