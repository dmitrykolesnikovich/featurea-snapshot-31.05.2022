package featurea.runtime

import kotlin.reflect.KClass

typealias ContainerConstructor = () -> ContainerBuilder

class ContainerBuilder {

    val awaitProxies = mutableListOf<KClass<out Any>>()
    internal var onInitBlock: ContainerBlock<Unit>? = null
        private set
    internal var onCreateBlock: ContainerBlock<Unit>? = null
        private set
    internal var onDestroyBlock: ContainerBlock<Unit>? = null
        private set

    fun onInit(block: ContainerBlock<Unit>) {
        onInitBlock = block
    }

    fun onCreate(block: ContainerBlock<Unit>) {
        onCreateBlock = block
    }

    fun onDestroy(block: ContainerBlock<Unit>) {
        onDestroyBlock = block
    }

    fun <T : Any> await(proxy: KClass<T>) {
        awaitProxies.add(proxy)
    }

}
