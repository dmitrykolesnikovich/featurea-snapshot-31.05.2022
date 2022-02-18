package featurea.config

import featurea.content.ContentType
import featurea.content.Resource
import featurea.pathWithoutExtension
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

class ConfigContentType(override val module: Module) : Component, ContentType {

    private val configContent: ConfigContent = import()

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        val name: String = resource.configPath.pathWithoutExtension
        configContent.findConfig(name)
        loadingQueue.add(resource.path)
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        // todo
        releaseQueue.add(resource.path)
    }

}
