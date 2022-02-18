package featurea.runtime

enum class ProvideType {
    INJECT,
    APPEND,
    REPLACE,
}

interface ComponentProvider<T> {
    fun provideComponent(module: Module)
}

@Constructor
fun <T : Any> ComponentProvider(init: ComponentProvider<T>.(module: Module) -> Unit): ComponentProvider<T> {
    return object : ComponentProvider<T> {
        override fun provideComponent(module: Module) {
            return init(module)
        }
    }
}
