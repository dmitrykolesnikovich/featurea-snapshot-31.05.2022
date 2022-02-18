package featurea.audio

import featurea.System
import featurea.jvm.readInputStreamOrNull
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.MP3Decoder
import javazoom.jl.decoder.OutputBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

actual class AudioEffect actual constructor(override val module: Module) : Component, AudioResource {

    private val audio: Audio = import()
    private val delegate: AudioDelegate = import()
    private val system: System = import()

    private var bufferID: Int = -1
    private var soundId: Long = -1
    private var duration: Double = 1.0
    private lateinit var filePath: String

    override fun init(filePath: String) {
        this.filePath = filePath
    }

    override fun load() {
        val output = ByteArrayOutputStream(4096)
        val bitstream = Bitstream(system.readInputStreamOrNull(filePath))
        val decoder = MP3Decoder()
        try {
            var outputBuffer: OutputBuffer? = null
            var sampleRate = -1
            var channels = -1
            while (true) {
                val header: Header = bitstream.readFrame() ?: break
                if (outputBuffer == null) {
                    channels = if (header.mode() == Header.SINGLE_CHANNEL) 1 else 2
                    outputBuffer = OutputBuffer(channels, false)
                    decoder.setOutputBuffer(outputBuffer)
                    sampleRate = header.getSampleRate()
                }
                try {
                    decoder.decodeFrame(header, bitstream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                bitstream.closeFrame()
                output.write(outputBuffer.buffer, 0, outputBuffer.reset())
            }
            bitstream.close()
            setup(output.toByteArray(), channels, sampleRate)
        } catch (ex: Throwable) {
            throw RuntimeException("Error reading audio data.", ex)
        }
    }

    override fun release() {
        if (bufferID != -1) {
            freeBuffer(bufferID);
            AL10.alDeleteBuffers(bufferID);
            bufferID = -1;
            delegate.forgetSound(this);
        }
    }

    actual fun play(isLoop: Boolean) {
        var sourceId: Int = delegate.obtainSource(false)
        if (sourceId == -1) {
            delegate.retainSound(this, true)
            sourceId = delegate.obtainSource(false)
        } else {
            delegate.retainSound(this, false)
        }
        if (sourceId == -1) return
        soundId = delegate.sourceToSoundId[sourceId] ?: -1
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferID)
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, if (isLoop) AL10.AL_TRUE else AL10.AL_FALSE)
        AL10.alSourcef(sourceId, AL10.AL_GAIN, audio.volume)
        AL10.alSourcePlay(sourceId)
    }

    actual fun stop() {
        if (!AL.isCreated()) return

        val sourceId = delegate.soundIdToSource[soundId] ?: error("Audio not found: $filePath")
        AL10.alSourceStop(sourceId)
    }

    actual fun adjustVolume(volume: Float) {
        if (!AL.isCreated()) return

        val sourceId = delegate.soundIdToSource[soundId] ?: error("Audio not found: $filePath")
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume)
    }

    private fun setup(pcm: ByteArray, channels: Int, sampleRate: Int) {
        val bytes = pcm.size - pcm.size % if (channels > 1) 4 else 2
        val samples = bytes / (2 * channels)
        duration = samples / sampleRate.toDouble()
        val buffer: ByteBuffer = BufferUtils.createByteBuffer(bytes).put(pcm, 0, bytes).flip() as ByteBuffer
        if (bufferID == -1) {
            // >> IMPORTANT if you expirience crash here like this:
            // Failed to flush core dump. Minidumps are not enabled by default on client versions of Windows
            // then it means that OpenAL.init() not called yet
            bufferID = AL10.alGenBuffers()
            // <<
            AL10.alBufferData(
                bufferID,
                if (channels > 1) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16,
                buffer.asShortBuffer(),
                sampleRate
            )
        }
    }

    actual fun pause() {
        if (!AL.isCreated()) return

        val sourceId = delegate.soundIdToSource[soundId] ?: error("Audio not found: $filePath")
        AL10.alSourcePause(sourceId)
    }

    actual fun resume() {
        if (!AL.isCreated()) return

        val sourceId = delegate.soundIdToSource[soundId] ?: error("Audio not found: $filePath")
        if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED) {
            AL10.alSourcePlay(sourceId)
        }
    }

    actual fun stopAll() {
        if (!AL.isCreated()) return

        val idleSources = delegate.idleSourcesIds
        val sources = delegate.sourceToSoundId
        var index = 0
        val size = idleSources.size
        while (index < size) {
            val sourceID = idleSources[index]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                val soundId = sources.remove(sourceID) ?: error("sources: $sources, sourceID: $sourceID")
                delegate.soundIdToSource.remove(soundId)
                AL10.alSourceStop(sourceID)
            }
            index++
        }
    }

    actual fun pauseAll() {
        if (!AL.isCreated()) return

        val idleSources = delegate.idleSourcesIds
        var index = 0
        val size = idleSources.size
        while (index < size) {
            val sourceID = idleSources[index]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                AL10.alSourcePause(sourceID)
            }
            index++
        }
    }

    actual fun resumeAll() {
        if (!AL.isCreated()) return

        val idleSources = delegate.idleSourcesIds
        var index = 0
        val size = idleSources.size
        while (index < size) {
            val sourceID = idleSources[index]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                if (AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED) {
                    AL10.alSourcePlay(sourceID)
                }
            }
            index++
        }
    }

    /*internals*/


    private fun freeBuffer(bufferID: Int) {
        val idleSources = delegate.idleSourcesIds
        val sources = delegate.sourceToSoundId
        val soundIdToSource = delegate.soundIdToSource

        var index = 0
        val size = idleSources.size
        while (index < size) {
            val sourceID = idleSources[index]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                if (sources.containsKey(sourceID)) {
                    val soundId: Long = sources.remove(sourceID) ?: error("sources: $sources, sourceID: $sourceID")
                    soundIdToSource.remove(soundId)
                }
                AL10.alSourceStop(sourceID)
                AL10.alSourcei(sourceID, AL10.AL_BUFFER, 0)
            }
            index++
        }
    }

}