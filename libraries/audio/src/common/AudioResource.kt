package featurea.audio

interface AudioResource {
    fun init(filePath: String)
    fun load()
    fun release()
}