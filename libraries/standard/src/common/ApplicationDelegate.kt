package featurea

interface ApplicationDelegate {

    // Application callbacks:
    fun create() {}
    fun start() {}
    fun resume() {}
    fun pause() {}
    fun stop() {}
    fun destroy() {}

    // Loader callback
    suspend fun load(progress: Float) {}

    // Window callbacks:
    fun update(elapsedTime: Float) {}
    fun resize(width: Int, height: Int) {}
    fun invalidate() {}

}

/*convenience*/

object DefaultApplicationDelegate : ApplicationDelegate // todo drop this concept

open class ApplicationContext : ApplicationComponent(), ApplicationDelegate
