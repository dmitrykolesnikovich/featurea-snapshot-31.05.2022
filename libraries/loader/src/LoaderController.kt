package featurea.loader

import featurea.Application
import featurea.ApplicationController
import featurea.content.Content
import featurea.content.ContentTypeRegistry
import featurea.content.Resource
import featurea.runtime.Module
import featurea.runtime.import

class LoaderController(module: Module) : ApplicationController(module) {

    private val app: Application = import()
    private val content: Content = import()
    private val contentTypeRegistry: ContentTypeRegistry = import()
    private val loader: Loader = import()

    var isActive = false
        private set
    private var loadingPool = mutableListOf<String>()
    private var releasePool = mutableListOf<String>()
    private var capacity: Float = 0f
    private val progress: Float get() = if (capacity == 0f) 0f else (capacity - loadingPool.size) / capacity
    private val loadingQueue = ArrayList<String>() // just for try todo replace with local variable
    private val releaseQueue = ArrayList<String>() // just for try todo replace with local variable

    fun enqueueLoadingPool(resourcesToLoad: List<String>) {
        loadingPool.addAll(resourcesToLoad)
        loadingPool = loadingPool.distinct().toMutableList()
    }

    fun enqueueReleasePool(resourcesToRelease: List<String>) {
        releasePool.addAll(resourcesToRelease)
        releasePool = releasePool.distinct().toMutableList()
    }

    override suspend fun update() {
        if (!isActive && loadingPool.isNotEmpty()) {
            activate()
        }

        if (isActive) {
            // 1. clear all releasePool
            if (releasePool.isNotEmpty()) {
                for (resourcePath in releasePool) {
                    val existingResource: Resource? = content.existingResources[resourcePath]
                    if (existingResource != null) {
                        contentTypeRegistry.releaseResource(existingResource, releaseQueue)
                        releaseQueue.distinct().forEach { releasePool.remove(it) }
                        releaseQueue.clear()
                    } else {
                        releasePool.remove(resourcePath) // just for now todo delete this
                    }
                }
            }

            // 2. load next resource from loadingPool
            if (loadingPool.isNotEmpty()) {
                val resourcePath: String = loadingPool[0]
                try {
                    val resource: Resource? = content.findResourceOrNull(resourcePath)
                    if (resource != null) {
                        contentTypeRegistry.loadResource(resource, loadingQueue)
                        loadingQueue.distinct().forEach { loadingPool.remove(it) }
                        loadingQueue.clear()
                    }
                } finally {
                    loadingPool.remove(resourcePath) // IMPORTANT because `palette-desktop.properties`
                }
            }

            // 3. inform ui that loading is progressed
            updateProgress(progress)
        }

        // >> quickfix todo improve
        if (isActive) {
            app.delegate.load(progress)
        }
        // <<
    }

    /*internals*/

    private fun activate() {
        isActive = true
        capacity = loadingPool.size.toFloat()
    }

    private fun complete() {
        isActive = false
        capacity = 0f
    }

    private suspend fun updateProgress(progress: Float) {
        for (listener in loader.listeners) {
            listener.update(progress)
        }
        if (progress == 1f) {
            // 1. listeners
            complete()
            for (listener in loader.listeners) {
                listener.complete()
            }

            // 2. repeat tasks
            for (task in app.tasksToRepeatOnBuildApplication) {
                task()
            }

            // 3. run tasks
            val tasks = app.tasksToRunOnCompleteLoading.swap()
            for (task in tasks) {
                task()
            }
            tasks.clear()
        }
    }

}
