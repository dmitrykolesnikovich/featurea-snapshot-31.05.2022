package featurea.audio

internal class AudioThread(private val audioDelegate: AudioDelegate) : Thread() {

    var isRunning = false

    override fun run() {
        while (isRunning) {
            audioDelegate.update()
        }
    }

    @Synchronized
    override fun start() {
        isRunning = true
        super.start()
    }

}
