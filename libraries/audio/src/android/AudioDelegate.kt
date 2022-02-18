package featurea.audio

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.create
import featurea.runtime.import

actual class AudioDelegate actual constructor(override val module: Module) : Component {

    private val mainActivity: FeatureaActivity = import(MainActivityProxy)

    val tracks = mutableListOf<AudioTrack>()
    lateinit var soundPool: SoundPool
    private lateinit var manager: AudioManager

    override fun onCreateComponent() {
        this.soundPool = newSoundPool(maxStreams = 5);
        this.manager = mainActivity.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onDeleteComponent() {
        for (track in tracks) {
            track.release()
        }
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
    }

    @Synchronized
    actual fun newAudioTrack(filePath: String): AudioTrack {
        val audioTrack = create<AudioTrack>().apply { init(filePath) }
        audioTrack.load()
        tracks.add(audioTrack)
        return audioTrack
    }

    @Synchronized
    actual fun newAudioEffect(filePath: String): AudioEffect {
        val audioEffect = create<AudioEffect>().apply { init(filePath) }
        audioEffect.load()
        return audioEffect
    }

}

/*internals*/

private fun newSoundPool(maxStreams: Int): SoundPool {
    return if (SDK_INT >= LOLLIPOP) {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder().setAudioAttributes(attributes).build()
    } else {
        SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
    }
}