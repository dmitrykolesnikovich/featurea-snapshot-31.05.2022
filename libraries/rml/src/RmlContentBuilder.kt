@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")

package featurea.rml

import featurea.content.ResourceTag
import featurea.content.UNDEFINED_RESOURCE_PATH
import featurea.rml.reader.RmlContent
import featurea.rml.reader.RmlFile
import featurea.runtime.Component
import featurea.runtime.DependencyRegistry
import featurea.runtime.Module
import featurea.runtime.import

suspend fun <T : Any> Component.buildResource(resourcePath: String, source: String? = null): T {
    val rmlContent: RmlContent = import()
    val rmlResource: RmlResource = rmlContent.readRmlResource(resourcePath, source)
    val resource: T = module.buildResource<T>(rmlResource)
    return resource
}

// quickfix todo refactor `buildResourceWithoutCache` to something more conceptual
suspend fun <T : Any> Component.buildResourceWithoutCache(idPath: String, source: String): T {
    val rmlContent: RmlContent = import()
    val rmlFile: RmlFile = RmlFile()
    rmlFile.init(source, UNDEFINED_RESOURCE_PATH) {
        rmlContent.findRmlSchema(packageId)
    }
    val property: ResourceTag? = rmlFile.rmlTag.findPropertyByIdPathOrNull(idPath)
    checkNotNull(property)
    val rmlResource: RmlResource = RmlResource().apply { rmlTag = property; this.rmlFile = rmlFile }
    val resource: T = module.buildResource<T>(rmlResource)
    return resource
}

fun RmlResource.initRmlBuilder(module: Module, init: RmlBuilderInit): RmlBuilder {
    val rmlResource: RmlResource = this
    val dependencyRegistry: DependencyRegistry = module.container.dependencyRegistry
    val builderCanonicalName: String = "${rmlResource.canonicalClassName}Builder"
    val builder: RmlBuilder = if (dependencyRegistry.moduleComponents.containsKey(builderCanonicalName)) {
        module.createComponent(builderCanonicalName) { /*intentionally blank for js*/ }
    } else {
        init()
    }
    rmlResource.builder = builder
    return rmlResource.builder ?: error("builderCanonicalName: $builderCanonicalName")
}

/*internals*/

private suspend fun <T : Any> Module.buildResource(rmlResource: RmlResource): T {
    val module: Module = this
    rmlResource.initRmlBuilder(module) {
        DefaultRmlResourceBuilder<T>(module) as RmlBuilder
    }
    val resource: T = with(rmlResource) { rmlTag.createRmlTagEndObject<T>() }
    return resource
}
