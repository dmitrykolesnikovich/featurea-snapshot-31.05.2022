package featurea.rml.writer

import featurea.Bundle
import featurea.System
import featurea.content.Content
import featurea.content.ResourceTag
import featurea.jvm.relativeTo
import featurea.rml.forEachResourceAttribute
import featurea.rml.readRmlResource
import featurea.rml.reader.RmlContent
import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import

// deserialize `rmlTag` from `rmlFile`
class RmlDeserializer(override val module: Module) : Component {

    private val content: Content = import()
    private val rmlContent: RmlContent = import()
    private val system: System = import()

    suspend fun deserialize(rmlFilePath: String, bundle: Bundle): ResourceTag {
        val relativePath = system.relativeTo(rmlFilePath)
        content.providedResources.add(relativePath)
        val rmlResource = rmlContent.readRmlResource(rmlFilePath)
        val rootRmlTag = rmlResource.rmlTag
        rootRmlTag.forEachResourceAttribute { resourceTag, key, value ->
            content.writeIfAccept(resourceTag, key, value, bundle)
        }
        return rootRmlTag
    }

}
