package featurea.cameraView

import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.window.WindowElement
import featurea.window.WindowElementProvider
import featurea.window.provideWindowElement

fun CameraElementProvider(module: Module) = module.provideWindowElement(object : WindowElementProvider<CameraView> {

    private val mainActivity: FeatureaActivity = module.importComponent(MainActivityProxy)

    override fun Component.createElementOrNull(view: CameraView): WindowElement? {
        val surfaceView = when {
            view.url.startsWith("http://") -> HttpCameraView(mainActivity).apply {
                initUrl(view.url)
                create(mainActivity)
                start(mainActivity)
            }
            view.url.startsWith("rtsp://") -> RtspCameraView(mainActivity).apply {
                initUrl(view.url)
                create(mainActivity)
            }
            else -> null
        } ?: return null
        return WindowElement(surfaceView)
    }

    override fun Component.destroyElement(element: WindowElement) {
        val surfaceView = element.surfaceView
        if (surfaceView is HttpCameraView) {
            surfaceView.stop(mainActivity)
            surfaceView.destroy(mainActivity)
        } else if (surfaceView is RtspCameraView) {
            surfaceView.destroy(mainActivity)
        }
    }

})
