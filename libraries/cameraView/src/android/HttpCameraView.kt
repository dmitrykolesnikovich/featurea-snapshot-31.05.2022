package featurea.cameraView

import android.annotation.SuppressLint
import android.app.Activity
import com.github.niqdev.mjpeg.DisplayMode
import com.github.niqdev.mjpeg.Mjpeg
import com.github.niqdev.mjpeg.MjpegSurfaceView
import featurea.android.ActivityListener
import featurea.android.FeatureaActivity

@SuppressLint("ViewConstructor")
class HttpCameraView(val mainActivity: FeatureaActivity) : MjpegSurfaceView(mainActivity, null) {

    private var url: String? = null
    var isStart = false
        private set
    private var username: String? = null
    private var password: String? = null

    private val activityLifecycleAdapter = object : ActivityListener {
        override fun onActivityResumed(activity: Activity) = start(activity)
        override fun onActivityPaused(activity: Activity) = stop(activity)
        override fun onActivityDestroyed(activity: Activity) = destroy(activity)
    }

    fun initUrl(url: String) {
        this.url = url
        val (username, password) = url.parseUsernameAndPassword()
        this.username = username
        this.password = password
    }

    fun create(activity: Activity) {
        if (activity != mainActivity) return
        activity.application.registerActivityLifecycleCallbacks(activityLifecycleAdapter)
    }

    fun start(activity: Activity) {
        if (activity != mainActivity) return
        if (!isStart) startPlayback()
    }

    fun stop(activity: Activity) {
        if (activity != mainActivity) return
        if (isStart) stopPlayback()
    }

    fun destroy(activity: Activity) {
        if (activity != mainActivity) return
        activity.application.unregisterActivityLifecycleCallbacks(activityLifecycleAdapter)
    }

    /*internals*/

    private fun startPlayback() {
        Mjpeg.newInstance()
            .credential(username, password)
            .open(url, 5 /*seconds*/)
            .subscribe(
                { inputStream ->
                    setSource(inputStream)
                    setDisplayMode(DisplayMode.BEST_FIT)
                    showFps(false)
                },
                { isStart = false },
                { isStart = true }
            )
    }

    override fun stopPlayback() {
        super.stopPlayback()
        isStart = false
    }

}

