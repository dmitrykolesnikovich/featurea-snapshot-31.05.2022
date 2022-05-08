package featurea.app

import featurea.runtime.import

interface ApplicationDelegate {

    fun create() {}
    fun start() {}
    fun resume() {}
    fun pause() {}
    fun stop() {}
    fun destroy() {}

    // Loader
    suspend fun load(progress: Float) {}

    // Window
    fun update(elapsedTime: Float) {}
    fun resize(width: Int, height: Int) {}
    fun invalidate() {}

}

/*convenience*/

open class ApplicationContext : ApplicationComponent(), ApplicationDelegate {
    val app: Application = import()
}
