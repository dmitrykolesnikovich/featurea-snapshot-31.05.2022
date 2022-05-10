package featurea.content

import featurea.Bundle
import featurea.runtime.Component
import featurea.runtime.ComponentListener
import featurea.runtime.Module
import featurea.utils.isInstrumentationEnabled

class Content(override val module: Module) : Component, ComponentListener {

    val existingResources = linkedMapOf<String, Resource>()
    val providedResources = linkedSetOf<String>()
    private val readers = linkedMapOf<String, ResourceReader>()
    private val writers = mutableListOf<ResourceWriter>() // todo refactor to from List to Map

    override fun provideComponent(canonicalName: String, component: Any) {
        if (component is ResourceReader) {
            val resourceCanonicalName: String = canonicalName.replace(".reader.", ".").removeSuffix("Reader")
            readers[resourceCanonicalName] = component
        }
        if (component is ResourceWriter) {
            writers.add(component)
        }
    }

    suspend fun writeIfAccept(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        var accept: Boolean = true
        for (writer in writers) {
            accept = accept && writer.accept(resourceTag, key, value, bundle)
        }
        if (accept) {
            for (writer in writers) {
                writer.write(resourceTag, key, value, bundle)
            }
        }
    }

    suspend fun flush(bundle: Bundle) {
        for (writer in writers) {
            writer.flush(bundle)
        }
    }

    suspend fun findResource(resourcePath: String, bundle: Bundle? = null): Resource {
        val resource: Resource? = findResourceOrNull(resourcePath, bundle)
        if (resource == null) {
            throw ResourceNotFoundException(resourcePath)
        }
        return resource
    }

    suspend fun findResourceOrNull(resourcePath: String, bundle: Bundle? = null): Resource? {
        // 0. instrument resource
        if (isInstrumentationEnabled) {
            for (reader in readers.values) {
                reader.createIfAbsent(resourcePath)
            }
        }

        // existing
        val existingResource: Resource? = existingResources[resourcePath]
        if (existingResource != null) {
            return existingResource
        }

        // newly created
        for ((resourceCanonicalName, reader) in readers) {
            val resource: Resource? = reader.readOrNull(resourcePath, bundle)
            if (resource != null) {
                resource.canonicalName = resourceCanonicalName
                resource.path = resourcePath
                existingResources[resourcePath] = resource
                return resource
            }
        }

        // 3. resource not found
        return null
    }

}
