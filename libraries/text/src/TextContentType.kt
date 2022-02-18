package featurea.text

import featurea.content.ContentType
import featurea.content.Resource
import featurea.runtime.Component
import featurea.runtime.Module

class TextContentType(override val module: Module) : Component, ContentType {

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        loadingQueue.add(resource.path)
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        releaseQueue.add(resource.path)
    }

}
