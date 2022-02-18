package featurea.audio

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import featurea.jvm.BufferFactory.createFloatBuffer
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.create
import org.lwjgl.LWJGLException
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import java.nio.FloatBuffer

private const val maxSourceCount: Int = 16
private val orientation: FloatBuffer = createFloatBuffer(6).put(floatArrayOf(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)).flip() as FloatBuffer
private val position: FloatBuffer = createFloatBuffer(3).put(floatArrayOf(0.0f, 0.0f, 0.0f)).flip() as FloatBuffer
private val velocity: FloatBuffer = createFloatBuffer(3).put(floatArrayOf(0.0f, 0.0f, 0.0f)).flip() as FloatBuffer

actual class AudioDelegate actual constructor(override val module: Module) : Component {

    private val sourceIds: ArrayList<Int> = ArrayList(maxSourceCount)
    lateinit var idleSourcesIds: ArrayList<Int>

    var tracks: ArrayList<AudioTrack> = ArrayList(1)
    private lateinit var audioThread: AudioThread

    val soundIdToSource = mutableMapOf<Long, Int>()
    val sourceToSoundId = mutableMapOf<Int, Long>()
    private var nextSoundId: Long = 0L
    private val recentSounds = ArrayList<AudioEffect>(maxSourceCount)
    private var mostRecentSound: Int = -1
    private var isInitialized: Boolean = false

    private fun init() {
        if (isInitialized) return
        isInitialized = true
        LwjglNativesLoader.load()
        if (!AL.isCreated()) {
            try {
                AL.create()
            } catch (e: LWJGLException) {
                e.printStackTrace()
            }
        }

        for (index in 0 until maxSourceCount) {
            val sourceId = AL10.alGenSources();
            if (AL10.alGetError() == AL10.AL_NO_ERROR) {
                sourceIds.add(sourceId)
            }
        }
        idleSourcesIds = ArrayList(sourceIds)
        AL10.alListener(AL10.AL_ORIENTATION, orientation)
        AL10.alListener(AL10.AL_VELOCITY, velocity)
        AL10.alListener(AL10.AL_POSITION, position)
    }

    override fun onDeleteComponent() {
        if (!isInitialized) return
        if (!AL.isCreated()) return
        try {
            for (index in tracks.indices) {
                val music = tracks[index]
                music.release()
            }
            var index = 0
            val size = sourceIds.size
            while (index < size) {
                val sourceID = sourceIds[index]
                val sourceState = AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE)
                if (sourceState != AL10.AL_STOPPED) AL10.alSourceStop(sourceID)
                AL10.alDeleteSources(sourceID)
                index++
            }
            sourceToSoundId.clear()
            soundIdToSource.clear()
            AL.destroy()
            while (AL.isCreated()) {
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    actual fun newAudioTrack(filePath: String): AudioTrack {
        init()
        val music = create<AudioTrack>().apply { init(filePath) }
        tracks.add(music)
        return music
    }

    actual fun newAudioEffect(filePath: String): AudioEffect {
        init()
        val audioEffect = create<AudioEffect>().apply { init(filePath) }
        return audioEffect
    }

    fun obtainSource(isMusic: Boolean): Int {
        if (!AL.isCreated()) return -1

        var index = 0
        val size = idleSourcesIds.size
        while (index < size) {
            val sourceId = idleSourcesIds[index]
            val state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE)
            if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
                if (isMusic) {
                    idleSourcesIds.removeAt(index)
                } else {
                    if (sourceToSoundId.containsKey(sourceId)) {
                        val soundId: Long =
                            sourceToSoundId[sourceId] ?: error("sourceToSoundId: $sourceToSoundId, sourceId: $sourceId")
                        sourceToSoundId.remove(sourceId)
                        soundIdToSource.remove(soundId)
                    }
                    val soundId = nextSoundId++
                    sourceToSoundId[sourceId] = soundId
                    soundIdToSource[soundId] = sourceId
                }
                AL10.alSourceStop(sourceId)
                AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0)
                AL10.alSourcef(sourceId, AL10.AL_GAIN, 1f)
                AL10.alSourcef(sourceId, AL10.AL_PITCH, 1f)
                AL10.alSource3f(sourceId, AL10.AL_POSITION, 0f, 0f, 1f)
                return sourceId
            }
            index++
        }
        return -1
    }

    fun reloadAudioThread() {
        if (::audioThread.isInitialized) {
            audioThread.isRunning = false
            while (audioThread.isAlive);
        }
        audioThread = AudioThread(this)
        audioThread.start()
    }

    fun update() {
        if (!AL.isCreated()) return
        for (index in tracks.indices) tracks[index].update()
    }

    fun retainSound(effect: AudioEffect, isStop: Boolean) {
        mostRecentSound++
        mostRecentSound %= /*recentSounds.size*/maxSourceCount
        if (isStop) recentSounds[mostRecentSound].stopAll()
        /*recentSounds[mostRecentSound] = sound*/recentSounds.add(effect)
    }

    fun forgetSound(effect: AudioEffect) {
        for (index in recentSounds.indices) if (recentSounds[index] === effect) recentSounds.removeAt(index)
    }

}
