package featurea.audio

import featurea.utils.MutableIntArray
import featurea.System
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.jvm.cacheIfNotExists
import featurea.jvm.normalizedPath
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

private val streamIds = MutableIntArray(8)

actual class AudioEffect actual constructor(override val module: Module) : Component, AudioResource {

    private val audio: Audio = import()
    private val delegate: AudioDelegate = import()
    private val mainActivity: FeatureaActivity = import(MainActivityProxy)
    private val system: System = import()

    private lateinit var filePath: String
    private var soundId: Int = -1

    override fun init(filePath: String) {
        this.filePath = filePath
    }

    override fun load() {
        try {
            val externalPath: String = "${mainActivity.cacheDir.normalizedPath}/$filePath"
            system.cacheIfNotExists(filePath, externalPath)
            val soundId = delegate.soundPool.load(externalPath, 1)
            this.soundId = soundId
        } catch (e: Exception) {
            e.printStackTrace()
            error("Sound not loaded: $filePath")
        }
    }

    override fun release() {
        delegate.soundPool.unload(soundId)
    }

    actual fun play(isLoop: Boolean) {
        if (streamIds.size == 8) streamIds.pop()
        val streamId = delegate.soundPool.play(soundId, audio.volume, audio.volume, 1, if (isLoop) -1 else 0, 1f)
        if (streamId != 0) {
            streamIds.add(streamId)
        }
    }

    actual fun stop() = stopAll()

    actual fun pause() = pauseAll()

    actual fun resume() = resumeAll()

    actual fun pauseAll() {
        delegate.soundPool.autoPause()
    }

    actual fun resumeAll() {
        delegate.soundPool.autoResume()
    }

    actual fun stopAll() {
        for (index in 0 until streamIds.size) {
            delegate.soundPool.stop(streamIds[index])
        }
    }

    actual fun adjustVolume(volume: Float) {
        for (streamId in streamIds) {
            delegate.soundPool.setVolume(streamId, volume, volume)
        }
    }

}
