package featurea.loader

interface LoaderListener {
    fun start() {}
    fun update(progress: Float) {}
    fun complete() {}
}
