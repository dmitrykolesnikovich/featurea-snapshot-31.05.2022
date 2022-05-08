package featurea.audio

import featurea.app.Application
import featurea.app.DestroyApplicationListener
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.delete
import featurea.runtime.import

class Audio(override val module: Module) : Component {

    private val app: Application = import()
    private val delegate: AudioDelegate = import()

    var isEnable: Boolean = true
    private val musics = mutableMapOf<String, AudioTrack>()
    private val sounds = mutableMapOf<String, AudioEffect>()

    var volume: Float = 1f
        set(value) {
            field = volume
            for ((_, track) in musics) {
                track.adjustVolume(value)
            }
            for ((_, effect) in sounds) {
                effect.adjustVolume(value)
            }
        }

    var isPlay: Boolean = false
        private set

    init {
        app.listeners.add(DestroyApplicationListener {
            delegate.delete()
        })
    }

    fun playSound(filePath: String, isLoop: Boolean = false) {
        if (!isEnable) return
        isPlay = true
        val effect = sounds[filePath] ?: error("filePath: $filePath")
        effect.play(isLoop)
    }

    fun playMusic(filePath: String, isLoop: Boolean = false) {
        if (!isEnable) return
        isPlay = true
        val track = musics[filePath] ?: error("filePath: $filePath")
        track.play(isLoop)
    }

    fun resumeSound(filePath: String) {
        if (!isEnable) return
        isPlay = true
        val effect = sounds[filePath] ?: error("filePath: $filePath")
        effect.resume()
    }

    fun resumeMusic(filePath: String) {
        if (!isEnable) return
        isPlay = true
        val track = musics[filePath] ?: error("filePath: $filePath")
        track.resume()
    }

    fun pauseSound(filePath: String) {
        if (!isEnable) return
        isPlay = false
        val effect = sounds[filePath] ?: error("filePath: $filePath")
        effect.pause()
    }

    fun pauseMusic(filePath: String) {
        if (!isEnable) return
        isPlay = false
        val track = musics[filePath] ?: error("filePath: $filePath")
        track.pause()
    }

    fun stopSound(filePath: String) {
        if (!isEnable) return
        isPlay = false
        val effect = sounds[filePath] ?: error("filePath: $filePath")
        effect.stop()
    }

    fun stopMusic(filePath: String) {
        if (!isEnable) return
        isPlay = false
        val track = musics[filePath] ?: error("filePath: $filePath")
        track.stop()
    }

    fun pauseAll() {
        for ((_, track) in musics) track.pause()
        for ((_, effect) in sounds) effect.pause()
    }

    fun resumeAll() {
        for ((_, track) in musics) track.resume()
        for ((_, effect) in sounds) effect.resume()
    }

    fun stopAll() {
        for ((_, track) in musics) track.stop()
        for ((_, effect) in sounds) effect.stop()
    }

    fun findSoundOrNull(filePath: String): AudioEffect? {
        return sounds[filePath]
    }

    fun findMusicOrNull(filePath: String): AudioTrack? {
        return musics[filePath]
    }

    fun findSoundOrCreate(filePath: String): AudioResource {
        var effect = sounds[filePath]
        if (effect == null) {
            effect = delegate.newAudioEffect(filePath)
            sounds[filePath] = effect
        }
        return effect
    }

    fun findMusicOrCreate(filePath: String): AudioTrack {
        var track = musics[filePath]
        if (track == null) {
            track = delegate.newAudioTrack(filePath)
            musics[filePath] = track
        }
        return track
    }

}
