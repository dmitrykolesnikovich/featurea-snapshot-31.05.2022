package featurea.runtime

enum class ProvideType {
    INJECT,
    APPEND,
    REPLACE,
}

interface ProxyProvider<T> {
    fun provideProxy(module: Module)
}

// constructor
fun <T : Any> ProxyProvider(init: ProxyProvider<T>.(module: Module) -> Unit): ProxyProvider<T> {
    return object : ProxyProvider<T> {
        override fun provideProxy(module: Module) {
            return init(module)
        }
    }
}
