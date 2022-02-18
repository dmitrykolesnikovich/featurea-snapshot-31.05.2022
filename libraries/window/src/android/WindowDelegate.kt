package featurea.window

import android.os.Build
import android.view.SurfaceView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import featurea.android.FeatureaActivity
import featurea.android.MainActivityContentViewProxy
import featurea.android.MainActivityProxy
import featurea.android.appendView
import featurea.layout.View
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlin.reflect.KClass

actual class WindowElement(val surfaceView: SurfaceView)

@RequiresApi(Build.VERSION_CODES.ECLAIR)
actual class WindowDelegate actual constructor(override val module: Module) : Component {

    private val mainActivity: FeatureaActivity = import(MainActivityProxy)
    private val mainActivityContentView: RelativeLayout = import(MainActivityContentViewProxy)
    private val window: Window = import()

    actual fun appendView(view: View) {
        val element: WindowElement = findWindowElementOrNull(view) ?: return
        val (x1, y1, x2, y2) = view.rectangle
        val x: Int = x1.toInt()
        val y: Int = y1.toInt()
        val width: Int = (x2 - x1).toInt()
        val height: Int = (y2 - y1).toInt()
        mainActivity.runOnUiThread {
            mainActivityContentView.appendView(element.surfaceView, x, y, width, height)
        }
    }

    actual fun removeView(view: View) {
        val element: WindowElement = window.elements.remove(view) ?: return
        val viewType: KClass<out View> = view::class
        val elementProvider: WindowElementProvider<View> = window.elementProviders[viewType] ?: return
        mainActivity.runOnUiThread {
            with(elementProvider) {
                destroyElement(element)
            }
            element.surfaceView.visibility = SurfaceView.GONE
            mainActivityContentView.removeView(element.surfaceView)
            mainActivityContentView.requestLayout()
            mainActivityContentView.invalidate()
        }
    }

    /*internals*/

    private fun findWindowElementOrNull(view: View): WindowElement? {
        // existing
        val existingElement: WindowElement? = window.elements[view]
        if (existingElement != null) {
            return existingElement
        }

        // newly created
        val viewType: KClass<out View> = view::class
        val elementProvider: WindowElementProvider<View> = window.elementProviders[viewType] ?: return null
        with(elementProvider) {
            val element: WindowElement = createElementOrNull(view) ?: return null
            element.surfaceView.setZOrderOnTop(true)
            window.elements[view] = element
            return element
        }
    }

}
