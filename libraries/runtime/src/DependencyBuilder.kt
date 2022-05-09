@file:Suppress("UNUSED_PARAMETER")

package featurea.runtime

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

class DependencyBuilder(val dependency: Dependency) {

    fun includeContentRoot(contentRoot: () -> String) {
        dependency.contentRoots.add(contentRoot)
    }

    fun includeContentRootWithConfig(contentRoot: () -> String) {
        includeContentRoot(contentRoot)
        dependency.useConfig = true
    }

    fun includeContentRootWithDefaultConfig(contentRoot: () -> String) {
        includeContentRootWithConfig(contentRoot)
        dependency.isDefaultConfigPackage = true
    }

    fun include(artifact: Dependency) {
        dependency.artifacts.add(artifact)
        if (artifact.useConfig) {
            dependency.resources.add(artifact)
        }
    }

    @JvmName("toModule")
    infix fun String.to(builder: () -> ModuleBuilder) {
        val canonicalName = createCanonicalName(this)
        dependency.modules[canonicalName] = builder
    }

    @JvmName("toContainer")
    infix fun String.to(builder: () -> ContainerBuilder) {
        val canonicalName = createCanonicalName(this)
        dependency.containers[canonicalName] = builder
    }

    @JvmName("toComponent")
    inline infix fun <reified T : Any> String.to(noinline componentConstructor: ComponentConstructor<T>) {
        val canonicalName = createCanonicalName(this)
        dependency.moduleComponents[canonicalName] = componentConstructor
        addCanonicalName(T::class, canonicalName)
    }

    @JvmName("toComponentProvider")
    inline infix fun <reified T : Any> String.to(noinline componentProvider: () -> ComponentProvider<T>) {
        // 1. register as dependency constructor
        this to { module -> componentProvider().apply { provideComponent(module) } }

        // 2. register as component provider
        val proxy = T::class
        dependency.componentProviders[proxy] = dependency.componentProvidersToken.toString()
    }

    @JvmName("toProxy")
    inline infix fun <reified T : Any> String.to(proxy: KClass<T>) {
        val canonicalName = createCanonicalName(this)
        addCanonicalName(T::class, canonicalName)
    }

    @Deprecated("Component constructor expected but default constructor used", ReplaceWith("Pair(this, that)"))
    infix fun <A, B> A.to(that: B): Pair<A, B> = Pair(this, that)

    fun static(block: StaticBlock) {
        dependency.staticBlocks.add(0, block) // quickfix todo improve
    }

}

/*convenience*/

inline fun <reified T : Any> DependencyBuilder.addComponent(simpleName: String, noinline component: ContainerBlock<T>) {
    val canonicalName = createCanonicalName(simpleName)
    dependency.containerComponents[canonicalName] = component
    addCanonicalName(T::class, canonicalName)
}

fun DependencyBuilder.createCanonicalName(simpleName: String): String {
    val canonicalName = "${dependency.artifactId}.${simpleName}"
    check(dependency.simpleNames.add(simpleName)) { "$canonicalName was created already" }
    return canonicalName
}

fun DependencyBuilder.addCanonicalName(type: KClass<*>, canonicalName: String) {
    dependency.componentProvidersToken.apply {
        clear()
        append(canonicalName)
    }
    dependency.canonicalNames[type] = canonicalName
}

// todo refactor `install(plugin)` to `plugin.install()`
inline fun <reified T : Any> DependencyBuilder.install(plugin: Plugin<T>) {
    ModulePlugin(this, T::class).plugin()
}
