package featurea.loader

import featurea.*
import featurea.app.Application
import featurea.content.*
import featurea.rml.RmlResource
import featurea.rml.buildApplication
import featurea.rml.forEachResourceAttribute
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.Task
import featurea.runtime.import
import featurea.text.TextContent
import featurea.utils.parseProperties

class Loader(override val module: Module) : Component {

    private val app: Application = import()
    private val content: Content = import()
    private val controller: LoaderController = import()
    private val contentTypeRegistry: ContentTypeRegistry = import()
    private val system: System = import()
    private val textContent: TextContent = import()

    val listeners = mutableListOf<LoaderListener>()
    private val loadedResources = mutableSetOf<String>()

    fun loadResource(resource: String, complete: Task? = null) {
        loadResources(listOf(resource), complete)
    }

    fun loadResources(resources: Iterable<String>, complete: Task? = null) {
        val resourcesThatAreNotReallyLoaded: List<String> = resources.distinct().filterNot { isResourceLoaded(it) }
        if (resourcesThatAreNotReallyLoaded.isNotEmpty()) {
            controller.enqueueLoadingPool(resourcesThatAreNotReallyLoaded)
            loadedResources.addAll(resourcesThatAreNotReallyLoaded)
        }
        if (complete != null) {
            app.tasksToRunOnCompleteLoading.add(complete)
        }
    }

    fun releaseResource(resource: String) {
        releaseResources(listOf(resource))
    }

    fun releaseResources(resources: Iterable<String>) {
        val resourcesThatAreLoadedIndeed: List<String> = resources.distinct().filter { isResourceLoaded(it) }
        controller.enqueueReleasePool(resourcesThatAreLoadedIndeed)
    }

    suspend fun loadBundle(bundlePath: String, complete: Task? = null) {
        // 1. filter
        check(content.providedResources.isEmpty())

        // 2. setup
        system.workingDir = bundlePath
        system.contentRoots.add(0, bundlePath) // quickfix todo avoid
        val manifestSource: String = textContent.findTextOrNull("manifest.properties") ?: error("manifest not found")
        val manifest: MutableMap<String, String> = parseProperties(manifestSource)
        val providedResources: List<String> = manifest["resources"]?.split(", ") ?: error("resources not found")
        system.properties.putAll(manifest)
        content.providedResources.addAll(providedResources)
        val mainProject: String = system.properties.mainProject
        val mainDocument: String = system.properties.mainDocument
        val screenPath: String = "$mainProject:/$mainDocument"

        // 3. action
        for (listener in listeners) {
            listener.start()
        }
        for (task in app.tasksToRepeatOnStartLoading) {
            task()
        }
        loadResources(content.providedResources) {
            app.delegate = module.buildApplication(screenPath)
            if (complete != null) {
                complete()
            }
        }
    }

    suspend fun loadRmlResource(rmlResource: RmlResource, complete: Task? = null) {
        rmlResource.rmlTag.forEachResourceAttribute { rmlTag, key, value ->
            loadRmlAttribute(rmlTag, key, value)
        }
        loadResources(content.providedResources, complete)
    }

    suspend fun loadRmlAttribute(rmlTag: ResourceTag, key: String, value: String, complete: Task? = null) {
        val resourcePaths: List<String>? = contentTypeRegistry.parseOrNull(rmlTag, key, value)
        if (resourcePaths != null) {
            loadResources(resourcePaths, complete)
        } else {
            val resource: Resource? = content.findResourceOrNull(value)
            if (resource != null) {
                loadResource(resource.path, complete)
            }
        }
    }

    /*internals*/

    private fun isResourceLoaded(resourcePath: String): Boolean {
        return loadedResources.contains(resourcePath)
    }

}
