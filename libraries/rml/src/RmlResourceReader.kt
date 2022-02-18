package featurea.rml

import featurea.content.ResourceTag
import featurea.rml.reader.RmlContent
import featurea.rml.reader.RmlFile
import featurea.runtime.Component
import featurea.runtime.import
import featurea.toFilePath
import featurea.toIdPath

suspend fun Component.readRmlResource(resourcePath: String, source: String? = null): RmlResource {
    val rmlContent: RmlContent = import()
    return rmlContent.readRmlResource(resourcePath, source)
}

suspend fun RmlContent.readRmlResource(resourcePath: String, source: String? = null): RmlResource {
    val filePath: String = resourcePath.toFilePath()
    val idPath: String? = resourcePath.toIdPath()
    val rmlFile: RmlFile = findRmlFile(filePath, source)
    val property: ResourceTag = rmlFile.rmlTag.findPropertyByIdPath(idPath)
    return RmlResource().apply {
        this.rmlTag = property
        this.rmlFile = rmlFile
    }
}
