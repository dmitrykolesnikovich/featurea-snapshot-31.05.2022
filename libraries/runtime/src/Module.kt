package featurea.runtime

import kotlin.reflect.KClass

typealias ModuleBlock = (Module) -> Unit

class ModuleNotFoundException(canonicalName: String) : RuntimeException(canonicalName)

class Module(val runtime: Runtime, val container: Container) : Component {

    override val module: Module = this
    val components = ComponentRegistry<Any>()
    internal var isInitComplete: Boolean = false
    lateinit var key: String
    private val registries = mutableListOf<ModuleRegistry<Any>>()
    val destroyListeners = mutableListOf<() -> Unit>()

    inline fun <reified T : Any> importComponent(): T {
        return importComponent(T::class)
    }

    fun <T : Any> importComponent(delegate: Delegate<T>): T {
        return importComponent(delegate.proxyType).delegate
    }

    fun <T : Any> importComponent(type: KClass<T>): T {
        val canonicalName: String = container.dependencyRegistry.findCanonicalName(type)
        return importComponent(type, canonicalName)
    }

    inline fun <reified T : Any> importComponent(canonicalName: String): T {
        val type: KClass<T> = T::class
        return importComponent(type, canonicalName)
    }

    fun <T : Any> importComponent(type: KClass<T>, canonicalName: String): T {
        // 1. existing component
        val existingComponent: T? = importComponentOrNull(type, canonicalName)
        if (existingComponent != null) {
            return existingComponent
        }

        // 2. new component
        components.pushTransaction(canonicalName)
        val component: T = loadComponent(type, canonicalName)
        components.pullTransaction(component)
        if (component is Component) {
            component.onCreateComponent()
        }
        if (component is ModuleRegistry<*>) {
            @Suppress("UNCHECKED_CAST")
            registries.add(component as ModuleRegistry<Any>)
        } else {
            installPlugin(plugin = component::class)
        }
        return component
    }

    inline fun <reified T : Any> createComponent(noinline init: T.() -> Unit = {}): T {
        val canonicalName: String = container.dependencyRegistry.findCanonicalName(T::class)
        return createComponent(canonicalName, init)
    }

    inline fun <reified T : Any> createComponent(canonicalName: String, init: T.() -> Unit = {}): T {
        val component: T = loadComponent(T::class, canonicalName)
        component.init()
        if (component is Component) {
            component.onCreateComponent()
        }
        return component
    }

    fun <T : Any> provideComponent(component: T) {
        val type = component::class
        val canonicalName = container.dependencyRegistry.findCanonicalName(type)
        components.inject(canonicalName, component)
        installPlugin(plugin = type)
    }

    fun <T : Any> importComponentOrNull(type: KClass<T>, canonicalName: String): T? {
        // existing
        val existingComponent: T? = components.getOrNull(canonicalName)
        if (existingComponent != null) {
            return existingComponent
        }

        // static
        val staticComponent: T? = container.findStaticOrNull<T>(canonicalName)
        if (staticComponent != null) {
            return staticComponent
        }

        // primary
        val primaryModule: Module? = runtime.moduleProvider.findPrimaryModuleOrNull(canonicalName)
        if (primaryModule != null) {
            return primaryModule.importComponent(type, canonicalName)
        }

        // included
        for ((_, includedModule) in runtime.moduleProvider.includedModules) {
            val includedComponent: T? = includedModule.components.getOrNull<T>(canonicalName)
            if (includedComponent != null) {
                return includedComponent
            }
        }

        // not found
        return null
    }

    fun <T : Any> loadComponent(type: KClass<T>, canonicalName: String): T {
        val dependencyRegistry: DependencyRegistry = container.dependencyRegistry
        val componentConstructor: ComponentConstructor<*>? = dependencyRegistry.moduleComponents[canonicalName]
        if (componentConstructor != null) {
            // 1. create component
            try {
                val component: Any? = componentConstructor(this)
                @Suppress("UNCHECKED_CAST")
                return component as T
            } catch (e: Exception) {
                e.printStackTrace()
                throw e // quickfix todo improve
            }
        } else {
            // 2. provide proxy
            val proxy: KClass<T> = type
            val componentProvider: String? = dependencyRegistry.componentProviders[proxy]
            if (componentProvider == null) {
                throw DependencyNotFoundException(canonicalName)
            }
            importComponent<Component>(componentProvider)
            val existingComponent: T? = components.getOrNull(canonicalName)
            if (existingComponent == null) {
                throw DependencyNotFoundException(canonicalName)
            }
            return existingComponent
        }
    }

    fun destroy() {
        container.removeModule(this)
    }

    fun onDestroy(block: ModuleBlock) {
        runtime.destroy(block)
    }

    override fun toString(): String = "Module($key)"

    /*internals*/

    internal fun installRegistries() {
        for (registry in registries) {
            val type: KClass<*> = registry::class
            val canonicalNames: List<String>? = container.dependencyRegistry.features[type]
            if (canonicalNames != null) {
                for (canonicalName in canonicalNames) {
                    val feature: Component = importComponent<Component>(canonicalName)
                    registry.registerComponent(canonicalName, feature)
                }
            }
        }
    }

    internal fun installPlugin(plugin: KClass<*>) {
        val canonicalNames: List<String>? = container.dependencyRegistry.features[plugin]
        if (canonicalNames != null) {
            for (canonicalName in canonicalNames) {
                importComponent<Component>(canonicalName)
            }
        }
    }

    internal fun onDestroy() {
        for (destroyModuleBlock in runtime.destroyModuleBlocks) {
            destroyModuleBlock(this)
        }
        runtime.moduleProvider.builder.destroyBlock?.invoke(this)
        for (component in components) {
            if (component is Component) {
                component.onDeleteComponent()
            }
        }
        for (destroyListener in destroyListeners) {
            destroyListener()
        }
    }

}

@Constructor
fun DefaultModule() = Module {}

@Constructor
fun Module(init: ModuleBuilder.() -> Unit): ModuleBuilder = ModuleBuilder().apply(init)

/*convenience*/

val Module.artifact: Dependency get() = container.artifact

interface ModuleRegistry<T : Any> {
    fun registerComponent(canonicalName: String, component: T)
}

inline fun <reified T : Any> Module.findComponent(): T {
    val type: KClass<T> = T::class
    val canonicalName: String = container.dependencyRegistry.findCanonicalName(type)
    val existingComponent: T? = components.getOrNull(canonicalName)
    if (existingComponent == null) {
        throw DependencyNotFoundException(type)
    }
    return existingComponent
}
