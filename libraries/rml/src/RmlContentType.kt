package featurea.rml

import featurea.content.ContentType
import featurea.content.Resource
import featurea.rml.reader.RmlContent
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.toFilePath

class RmlContentType(override val module: Module) : Component, ContentType {

    private val rmlContent: RmlContent = import()

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        val filePath: String = resource.path.toFilePath()
        rmlContent.findRmlFile(filePath)
        loadingQueue.add(resource.path)
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        // todo
        releaseQueue.add(resource.path)
    }

}
