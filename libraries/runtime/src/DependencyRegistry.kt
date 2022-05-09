@file:Suppress("FoldInitializerAndIfToElvis")

package featurea.runtime

import kotlin.reflect.KClass

class DependencyRegistry private constructor(val artifact: Dependency) {

    val componentProviders = linkedMapOf<KClass<out Any>, String>()
    val features = mutableMapOf<KClass<*>, MutableList<String>>()
    val moduleComponents = linkedMapOf<String, ComponentConstructor<*>>()
    val containerComponents = linkedMapOf<String, ContainerBlock<Any>>()
    val containers = linkedMapOf<String, ContainerConstructor>()
    val modules = linkedMapOf<String, ModuleConstructor>()
    val moduleCanonicalNames = linkedMapOf<KClass<*>, String>()
    val canonicalNames = linkedMapOf<KClass<*>, String>()

    fun findCanonicalName(type: KClass<*>): String {
        val canonicalName = canonicalNames[type]
        if (canonicalName == null) {
            throw DependencyNotFoundException(type)
        }
        return canonicalName
    }

    // quickfix todo avoid companion object
    companion object {
        fun fromDependency(dependency: Dependency): DependencyRegistry {
            // dependency
            val namespaces = linkedSetOf<Dependency>()
            dependency.artifacts.add(DefaultArtifact())
            fun initRecursively(dependency: Dependency) {
                for (artifact in dependency.artifacts) {
                    initRecursively(artifact)
                }
                namespaces.add(dependency)
            }
            initRecursively(dependency)

            // dependencyRegistry
            val dependencyRegistry: DependencyRegistry = DependencyRegistry(dependency).apply {
                for (namespace in namespaces) {
                    moduleComponents.putAll(namespace.moduleComponents)
                    containerComponents.putAll(namespace.containerComponents)
                    modules.putAll(namespace.modules)
                    containers.putAll(namespace.containers)
                    componentProviders.putAll(namespace.componentProviders)
                    canonicalNames.putAll(namespace.canonicalNames)
                    for ((pluginDependency, featureCanonicalNames) in namespace.features) {
                        features.getOrPut(pluginDependency) { mutableListOf() }.addAll(featureCanonicalNames)
                    }
                }
            }

            // features
            for ((_, features) in dependencyRegistry.features) {
                features.sortWith(providersFirstComparator)
            }

            return dependencyRegistry
        }
    }

}

/*internals*/

private val providersFirstComparator: Comparator<String> = Comparator<String> { first, second ->
    val feature1: String = first.simpleName
    val feature2: String = second.simpleName
    when {
        feature2.startsWith("provide") -> 1
        feature1.startsWith("provide") -> -1
        else -> 1
    }
}

private val String.simpleName: String get() = split(".").last()
