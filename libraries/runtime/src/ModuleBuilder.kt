package featurea.runtime

import kotlin.reflect.KClass

typealias ModuleConstructor = () -> ModuleBuilder

class ModuleBuilder {

    internal val awaitProxies = mutableListOf<KClass<out Any>>()
    internal var initBlock: (InitBlock.(module: Module) -> Unit)? = null
        private set
    internal var createBlock: ModuleBlock? = null
        private set
    internal var destroyBlock: ModuleBlock? = null
        private set

    fun onInit(block: InitBlock.(module: Module) -> Unit) {
        initBlock = block
    }

    fun onCreate(block: ModuleBlock) {
        createBlock = block
    }

    fun onDestroy(block: ModuleBlock) {
        destroyBlock = block
    }

    inner class InitBlock {
        fun await(proxy: KClass<out Any>) {
            awaitProxies.add(proxy)
        }
    }

}
