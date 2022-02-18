package featurea.cameraView

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import com.pedro.vlc.VlcListener
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.*

internal class VlcVideoLibrary : MediaPlayer.EventListener {

    private val vlcListener: VlcListener
    private val surfaceView: SurfaceView
    private var vlc: LibVLC // https://stackoverflow.com/a/42670642/909169
    lateinit var mediaPlayer: MediaPlayer

    constructor(context: Context, vlcListener: VlcListener, surfaceView: SurfaceView) {
        this.vlcListener = vlcListener
        this.surfaceView = surfaceView
        this.vlc = LibVLC(context, ArrayList())
    }

    fun play(url: String?) {
        if (::mediaPlayer.isInitialized && !mediaPlayer.isReleased) {
            if (!mediaPlayer.isPlaying) mediaPlayer.play()
        } else {
            setMedia(Media(vlc, Uri.parse(url)))
        }
    }

    fun stop() {
        mediaPlayer.scale = 0f
        mediaPlayer.pause()
        mediaPlayer.stop()
        mediaPlayer.setVideoTrackEnabled(false)
        mediaPlayer.vlcVout.detachViews()
        mediaPlayer.vlcVout.setWindowSize(0, 0)
        mediaPlayer.media = null
        mediaPlayer.release()
    }

    override fun onEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.Playing -> vlcListener.onComplete()
            MediaPlayer.Event.EncounteredError -> vlcListener.onError()
        }
    }

    /*internals*/

    private fun setMedia(media: Media) {
        media.setHWDecoderEnabled(true, false)
        mediaPlayer = MediaPlayer(vlc).apply {
            this.media = media
            setEventListener(this@VlcVideoLibrary)
            setVideoTrackEnabled(true)
        }.also {
            val vlcVout: IVLCVout = it.vlcVout
            vlcVout.setVideoView(surfaceView)
            val width: Int = surfaceView.width
            val height: Int = surfaceView.height
            if (width != 0 && height != 0) {
                vlcVout.setWindowSize(width, height)
            }
            vlcVout.attachViews()
            it.play()
        }
    }

}
