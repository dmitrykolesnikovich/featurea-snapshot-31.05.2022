package featurea.audio

import featurea.System
import featurea.jvm.readInputStreamOrNull
import featurea.utils.log
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import javazoom.jl.decoder.*
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.lang.System as JvmSystem

actual class AudioTrack actual constructor(override val module: Module) : Component, AudioResource {

    private val delegate: AudioDelegate = import()
    private val system: System = import()

    private val bufferSize = 4096 * 10
    private val bufferCount = 3
    private val bytesPerSample = 2
    private val tempBytes = ByteArray(bufferSize)
    private val tempBuffer: ByteBuffer = BufferUtils.createByteBuffer(bufferSize)
    private lateinit var filePath: String
    private var buffers: IntBuffer? = null
    private var sourceId = -1
    private var format: Int = 0
    private var sampleRate: Int = 0
    private var isLoop: Boolean = false
    private var isPlaying: Boolean = false
    private var renderedSeconds: Double = 0.0
    private var secondsPerBuffer: Double = 0.0
    private lateinit var bitStream: Bitstream
    private lateinit var outputBuffer: OutputBuffer
    private lateinit var decoder: MP3Decoder
    private var isPaused = false

    override fun init(filePath: String) {
        this.filePath = filePath
    }

    override fun load() = reset()

    override fun release() {
        if (buffers == null) return;
        if (sourceId != -1) {
            reset();
            delegate.tracks.remove(this)
            freeSource(sourceId);
            sourceId = -1;
        }
        AL10.alDeleteBuffers(buffers)
        buffers = null;
    }

    actual fun play(isLoop: Boolean) {
        this.isLoop = isLoop
        if (sourceId == -1) {
            sourceId = delegate.obtainSource(isMusic = true)
            if (sourceId == -1) return
            if (buffers == null) {
                buffers = BufferUtils.createIntBuffer(bufferCount)
                AL10.alGenBuffers(buffers)
                if (AL10.alGetError() != AL10.AL_NO_ERROR) error("Unable to allocate audio buffers")
            }
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, if (isLoop) AL10.AL_TRUE else AL10.AL_FALSE)
            var filled = 0
            for (i in 0 until bufferCount) {
                val bufferID = buffers!![i]
                if (!fill(bufferID)) break
                filled++
                AL10.alSourceQueueBuffers(sourceId, bufferID)
            }
            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                log("alGetError = " + AL10.alGetError())
            }
        }
        AL10.alSourcePlay(sourceId)
        isPlaying = true
        isPaused = false
        delegate.reloadAudioThread()
    }

    actual fun pause() {
        if (sourceId != -1) {
            AL10.alSourcePause(sourceId)
        }
        isPlaying = false;
        isPaused = true;
    }

    actual fun stop() {
        if (sourceId == -1) return
        reset()
        freeSource(sourceId)
        sourceId = -1
        renderedSeconds = 0.0
        isPlaying = false
    }

    actual fun resume() = play(false)

    actual fun adjustVolume(volume: Float) {
        if (sourceId != -1) {
            AL10.alSourcef(sourceId, AL10.AL_GAIN, volume)
        }
    }

    @Synchronized
    fun update() {
        if (sourceId == -1) return
        var end = false
        var buffers = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED)
        while (buffers-- > 0) {
            val bufferID = AL10.alSourceUnqueueBuffers(sourceId)
            if (bufferID == AL10.AL_INVALID_VALUE) break
            renderedSeconds += secondsPerBuffer
            if (end) continue
            if (fill(bufferID)) {
                AL10.alSourceQueueBuffers(sourceId, bufferID)
            } else {
                end = true
            }
        }
        if (end && AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED) == 0) {
            stop()
        }
        if (isPlaying && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
            AL10.alSourcePlay(sourceId)
        }
    }

    /*internals*/

    @Synchronized
    private fun reset() {
        if (::bitStream.isInitialized) {
            try {
                bitStream.close()
            } catch (e: BitstreamException) {
                e.printStackTrace()
            }
        }
        bitStream = Bitstream(system.readInputStreamOrNull(filePath))
        decoder = MP3Decoder()
        try {
            val header = bitStream.readFrame()
            if (header != null) {
                val channels = if (header.mode() == Header.SINGLE_CHANNEL) 1 else 2
                outputBuffer = OutputBuffer(channels, false)
                decoder.setOutputBuffer(outputBuffer)
                format = if (channels == 2) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16
                sampleRate = header.sampleRate
                secondsPerBuffer = bufferSize.toDouble() / bytesPerSample / channels / sampleRate
            } else {
                error("empty mp3")
            }
        } catch (e: BitstreamException) {
            e.printStackTrace()
        }
    }

    private fun fill(bufferID: Int): Boolean {
        tempBuffer.clear()
        var length = read(tempBytes)
        if (length <= 0) {
            if (isLoop) {
                reset()
                renderedSeconds = 0.0
                length = read(tempBytes)
                if (length <= 0) return false
            } else {
                return false
            }
        }
        tempBuffer.put(tempBytes, 0, length).flip()
        AL10.alBufferData(bufferID, format, tempBuffer, sampleRate)
        return true
    }

    private fun freeSource(sourceId: Int) {
        if (!AL.isCreated()) return

        val idleSources = delegate.idleSourcesIds

        AL10.alSourceStop(sourceId)
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0)
        idleSources.add(sourceId)
    }

    private fun read(buffer: ByteArray): Int {
        return try {
            var totalLength = 0
            val minRequiredLength = buffer.size - OutputBuffer.BUFFERSIZE * 2
            while (totalLength <= minRequiredLength) {
                val header: Header = bitStream.readFrame()
                try {
                    decoder.decodeFrame(header, bitStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                bitStream.closeFrame()
                val length = outputBuffer.reset()
                JvmSystem.arraycopy(outputBuffer.buffer, 0, buffer, totalLength, length)
                totalLength += length
            }
            totalLength
        } catch (e: Throwable) {
            reset()
            e.printStackTrace()
            error(e)
        }
    }

}
