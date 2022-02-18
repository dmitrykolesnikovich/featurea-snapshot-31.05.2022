package featurea.input

import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.widget.RelativeLayout
import featurea.android.*
import featurea.runtime.*

@Provide(MainSurfaceViewProxy::class)
@Provide(RootLayoutProxy::class)
class MainView(override val module: Module) : Component, GLSurfaceView(module.importComponent(MainActivityProxy)) {

    private val mainActivityContentView: RelativeLayout = import(MainActivityContentViewProxy)
    private val touchEventProducer: TouchEventProducer = import()

    init {
        isFocusable = false
        isFocusableInTouchMode = false
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        setOnLongClickListener(touchEventProducer)
        setRenderer(import(MainRenderProxy))
        setZOrderOnTop(true) // IMPORTANT do not delete this
        provide(MainSurfaceViewProxy(this))
    }

    override fun onCreateComponent() {
        val rootLayout = linearLayoutOf(this)
        mainActivityContentView.addView(rootLayout)
        provide(RootLayoutProxy(rootLayout))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        touchEventProducer.onTouchEvent(event)
        return true
    }

}
