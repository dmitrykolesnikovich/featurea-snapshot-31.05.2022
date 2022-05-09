@file:Suppress("RemoveExplicitTypeArguments", "UNCHECKED_CAST")

package featurea.app

import featurea.rml.*
import featurea.rml.reader.RmlContent
import featurea.runtime.Module

typealias ApplicationBuilder = DefaultRmlResourceBuilder<ApplicationDelegate>

suspend fun Module.buildApplication(resourcePath: String, source: String? = null): ApplicationDelegate {
    val rmlContent: RmlContent = importComponent()
    val rmlResource: RmlResource = rmlContent.readRmlResource(resourcePath, source)
    val appDelegate: ApplicationDelegate = buildApplication(rmlResource)
    return appDelegate
}

suspend fun Module.buildApplication(rmlResource: RmlResource): ApplicationDelegate {
    val module: Module = this
    val builder: RmlBuilder = rmlResource.initRmlBuilder(module) {
        ApplicationBuilder(module) as RmlBuilder
    }
    val resource: Any = with(rmlResource) { rmlTag.createRmlTagEndObject<Any>() }
    val result: Any = builder.wrap(rmlResource, rmlResource.rmlTag, resource)
    val appDelegate: ApplicationDelegate = result as ApplicationDelegate
    return appDelegate
}
