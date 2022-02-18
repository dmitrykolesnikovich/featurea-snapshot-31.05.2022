package featurea.runtime

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

typealias Plugin<T> = ModulePlugin<T>.() -> Unit

class ModulePlugin<T : Any>(val dependencyBuilder: DependencyBuilder, val type: KClass<T>) {

    @JvmName("toFeature")
    infix fun String.to(action: Action) {
        val key: String = this
        val constructor: ComponentConstructor<Unit> = { module ->
            module.action()
        }
        key to constructor
    }

    inline infix fun <reified T : Any> String.to(noinline constructor: ComponentConstructor<T>) {
        val key: String = this
        with(dependencyBuilder) {
            key to constructor
            val canonicalNames = dependency.features.getOrPut(type) { mutableListOf() }
            val lastCanonicalName = dependency.componentProvidersToken.toString()
            check(!canonicalNames.contains(lastCanonicalName))
            canonicalNames.add(lastCanonicalName)
        }
    }

    @JvmName("toContainerBlock")
    inline infix fun <reified T : Any> String.to(noinline constructor: ContainerBlock<T>) {
        val key: String = this
        with(dependencyBuilder) {
            addComponent(key, constructor)
            val canonicalNames = dependency.features.getOrPut(type) { mutableListOf() }
            val lastCanonicalName = dependency.componentProvidersToken.toString()
            check(!canonicalNames.contains(lastCanonicalName))
            canonicalNames.add(lastCanonicalName)
        }
    }

    inline infix fun <reified T : Any> String.to(noinline componentProvider: () -> ComponentProvider<T>) {
        val key: String = this
        with(dependencyBuilder) {
            key to componentProvider
            val canonicalNames = dependency.features.getOrPut(type) { mutableListOf() }
            val lastCanonicalName = dependency.componentProvidersToken.toString()
            check(!canonicalNames.contains(lastCanonicalName))
            canonicalNames.add(lastCanonicalName)
        }
    }

}
