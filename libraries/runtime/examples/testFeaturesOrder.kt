package featurea.runtime.examples

import featurea.runtime.*

fun testFeaturesOrder() {
    class Service
    fun DependencyBuilder.Plugin(plugin: Plugin<Service>) = install(plugin)
    val components = DefaultArtifact {
        Plugin {
            "b" to ::DefaultComponent
            "d" to ::DefaultComponent
            "provideX" to ::DefaultComponent
            "provideT" to ::DefaultComponent
            "c" to ::DefaultComponent
            "a" to ::DefaultComponent
            "provideZ" to ::DefaultComponent
            "provideU" to ::DefaultComponent
            "w" to ::DefaultComponent
            "e" to ::DefaultComponent
        }
    }
    val dependencyRegistry: DependencyRegistry = DependencyRegistry.fromDependency(components)
    println(dependencyRegistry.features)
}