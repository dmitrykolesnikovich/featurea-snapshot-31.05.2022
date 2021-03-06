package featurea.runtime.examples

import featurea.runtime.*

fun testFeaturesOrderProvidersFirst() {
    class Service
    fun DependencyBuilder.ServicePlugin(plugin: Plugin<Service>) = install(plugin)
    val components = DefaultArtifact {
        ServicePlugin {
            "b" to ::DefaultComponent
            "d" to ::DefaultComponent
            "provideC" to ::DefaultComponent
            "provideA" to ::DefaultComponent
            "c" to ::DefaultComponent
            "a" to ::DefaultComponent
            "provideD" to ::DefaultComponent
            "provideB" to ::DefaultComponent
            "w" to ::DefaultComponent
            "e" to ::DefaultComponent
        }
    }
    val dependencyRegistry: DependencyRegistry = DependencyRegistry.fromDependency(components)
    println(dependencyRegistry.features[Service::class])
}
