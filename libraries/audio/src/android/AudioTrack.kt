package featurea.audio

import android.media.MediaPlayer
import featurea.android.MainActivityProxy
import featurea.jvm.cacheIfNotExists
import featurea.jvm.normalizedPath
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.System

actual class AudioTrack actual constructor(override val module: Module) : Component, AudioResource {

    private val delegate: AudioDelegate = import()
    private val mainActivity = import(MainActivityProxy)
    private val system: System = import()

    private lateinit var filePath: String
    var mediaPlayer: MediaPlayer? = null
    var wasPlaying = false
    private var isPrepared = true
    private var isPaused: Boolean = false

    override fun init(filePath: String) {
        this.filePath = filePath
    }

    override fun load() {
        try {
            val externalPath = "${mainActivity.cacheDir.normalizedPath}/$filePath"
            system.cacheIfNotExists(filePath, externalPath)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(externalPath)
                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error("Music not loaded: $filePath")
        }
    }

    @Synchronized
    override fun release() {
        val mediaPlayer = mediaPlayer ?: return
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            this.mediaPlayer = null
            delegate.tracks.remove(this)
        }
    }

    actual fun play(isLoop: Boolean) {
        val mediaPlayer = mediaPlayer ?: return
        isPaused = false
        if (mediaPlayer.isPlaying) return
        try {
            if (!isPrepared) {
                mediaPlayer.prepare()
                isPrepared = true
            }
            mediaPlayer.isLooping = isLoop
            mediaPlayer.start()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    actual fun pause() {
        val mediaPlayer = mediaPlayer ?: return
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPaused = true
        }
        wasPlaying = false
    }

    actual fun stop() {
        val mediaPlayer = mediaPlayer ?: return
        if (isPrepared) {
            mediaPlayer.seekTo(0)
        }
        mediaPlayer.stop()
        isPrepared = false
    }

    actual fun resume() {
        val mediaPlayer = mediaPlayer ?: return
        if (wasPlaying) play(mediaPlayer.isLooping)
    }

    actual fun adjustVolume(volume: Float) {
        val mediaPlayer = mediaPlayer ?: return
        mediaPlayer.setVolume(volume, volume)
    }

}
