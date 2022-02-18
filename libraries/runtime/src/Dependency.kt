package featurea.runtime

import kotlin.reflect.KClass

class DependencyNotFoundException : RuntimeException {
    constructor(canonicalName: String) : super(canonicalName)
    constructor(type: KClass<*>) : super(type.toString())
}

// todo replace `linkedSetOf` with `mutableListOf` by redoing `Artifact` logic
class Dependency internal constructor(val artifactId: String) {
    val artifacts = linkedSetOf<Dependency>()
    val resources = linkedSetOf<Dependency>()
    val contentRoots = mutableListOf<() -> String>()
    val moduleComponents = mutableMapOf<String, ComponentConstructor<Any>>()
    val containerComponents = mutableMapOf<String, ContainerBlock<Any>>()
    val componentProviders = linkedMapOf<KClass<out Any>, String>()
    val componentProvidersToken = StringBuilder() // quickfix todo improve
    val modules = mutableMapOf<String, () -> ModuleBuilder>()
    val containers = mutableMapOf<String, () -> ContainerBuilder>()
    val staticBlocks = mutableListOf<StaticBlock>()
    val features = mutableMapOf<KClass<*>, MutableList<String>>()
    val simpleNames = mutableSetOf<String>()
    val canonicalNames = linkedMapOf<KClass<out Any>, String>()
    var useConfig: Boolean = false
        internal set
    var isDefaultConfigPackage: Boolean = false
        internal set

    override fun toString(): String = "Dependency($artifactId)"
}

@Constructor
fun Artifact(id: String, includes: DependencyBuilder.() -> Unit = {}): Dependency = Dependency(id).apply {
    val dependencyBuilder = DependencyBuilder(this)
    dependencyBuilder.includes()
    if (useConfig) {
        resources.add(this)
    }
    resources.addAll(artifacts.flatMap { it.resources }.filter { it.useConfig })

    // >> quickfix todo improve
    staticBlocks.addAll(0, artifacts.flatMap { it.staticBlocks })
    val distinct = staticBlocks.distinct()
    staticBlocks.clear()
    staticBlocks.addAll(distinct)
    // <<
}

@Constructor
fun DefaultArtifact(includes: DependencyBuilder.() -> Unit = {}) = Artifact("featurea.runtime") {
    includes()

    "Container" to Container::class
    "DefaultComponent" to ::DefaultComponent
    "DefaultContainer" to ::DefaultContainer
    "DefaultModule" to ::DefaultModule
}

/*convenience*/

// quickfix todo improve
fun Dependency.findContentRootsRecursively(): List<String> {
    val result = mutableListOf<String>()
    for (contentRoot in contentRoots) {
        val contentRootNormalized: String = contentRoot().replace("\\", "/").replace("//", "/") // quickfix todo avoid
        result.add(contentRootNormalized)
    }
    for (artifact in artifacts) {
        result.addAll(artifact.findContentRootsRecursively())
    }
    return result
}

val Dependency.configPackages: List<String> get() = resources.map { if (it.isDefaultConfigPackage) "" else it.artifactId }
