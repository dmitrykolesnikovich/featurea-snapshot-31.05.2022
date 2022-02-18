package featurea.runtime

open class ComponentRegistry<T> : Iterable<T> {

    private val components = LinkedHashMap<String, T>()
    private var componentKey: String? = null
    private var componentCounter: Long = 0

    operator fun <T> get(key: String): T {
        return getOrNull(key) ?: error("key: $key")
    }

    fun <T> getOrNull(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return components[key] as T
    }

    operator fun <T> get(key: String, default: T): T {
        @Suppress("UNCHECKED_CAST")
        return (components[key] ?: default) as T
    }

    override fun iterator(): Iterator<T> {
        return components.values.iterator()
    }

    fun append(key: String, component: T) {
        componentCounter++
        val componentKey: String = "$key$componentCounter"
        inject(componentKey, component)
    }

    fun inject(key: String, component: T) {
        if (containsKey(key)) {
            error("key: $key")
        }
        if (component is Container) {
            component.key = key
            component.components.pushTransaction(CONTAINER_COMPONENT_KEY)
            component.components.pullTransaction(component)
        }
        if (component is Module) {
            component.key = key
            component.components.pushTransaction(MODULE_COMPONENT_KEY)
            component.components.pullTransaction(component)
        }
        components[key] = component
    }

    fun remove(key: String): T? {
        return components.remove(key)
    }

    fun containsKey(key: String): Boolean {
        return components.keys.contains(key)
    }

    fun contains(component: T): Boolean {
        return components.values.contains(component)
    }

    fun pushTransaction(componentKey: String) {
        this.componentKey = componentKey
    }

    fun pullTransaction(component: T) {
        val componentKey: String = checkNotNull(componentKey)
        if (components.isEmpty()) {
            when (component) {
                is Module -> check(componentKey == MODULE_COMPONENT_KEY)
                is Container -> check(componentKey == CONTAINER_COMPONENT_KEY)
                else -> error("$componentKey: $component")
            }
        }
        if (components[componentKey] == null) {
            components[componentKey] = component
        }
    }

}

/*internals*/

private const val CONTAINER_COMPONENT_KEY: String = "container"
private const val MODULE_COMPONENT_KEY: String = "module"
