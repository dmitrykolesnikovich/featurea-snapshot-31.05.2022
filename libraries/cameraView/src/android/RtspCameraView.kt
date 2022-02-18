package featurea.cameraView

import android.app.Activity
import android.view.SurfaceHolder
import android.view.SurfaceView
import featurea.android.ActivityListener
import featurea.android.SurfaceHolderListener

class RtspCameraView(val mainActivity: Activity) : SurfaceView(mainActivity) {

    private var vlcVideoLibrary: VlcVideoLibrary? = null
    private lateinit var url: String

    private val activityLifecycleAdapter = object : ActivityListener {
        override fun onActivityPaused(activity: Activity) = stop()
        override fun onActivityDestroyed(activity: Activity) = destroy(activity)
    }

    private val surfaceHolderCallback = object : SurfaceHolderListener {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = start()
    }

    private val vlcAdapter = object : VlcAdapter() {
        override fun onError() = stop()
    }

    fun initUrl(url: String) {
        this.url = url
    }

    fun create(activity: Activity) {
        if (activity != mainActivity) return
        activity.application.registerActivityLifecycleCallbacks(activityLifecycleAdapter)
        holder.addCallback(surfaceHolderCallback)
    }

    fun destroy(activity: Activity) {
        if (activity != mainActivity) return
        activity.application.unregisterActivityLifecycleCallbacks(activityLifecycleAdapter)
        holder.removeCallback(surfaceHolderCallback)
    }

    /*internals*/

    private fun start() {
        vlcVideoLibrary = VlcVideoLibrary(mainActivity, vlcAdapter, this).also {
            it.play(url)
            it.mediaPlayer.vlcVout.setWindowSize(width, height)
        }
    }

    private fun stop() {
        vlcVideoLibrary?.stop()
        vlcVideoLibrary = null
    }

}
