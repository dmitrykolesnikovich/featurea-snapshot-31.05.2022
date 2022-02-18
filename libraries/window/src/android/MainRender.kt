package featurea.window

import android.opengl.GLSurfaceView
import featurea.android.MainRenderProxy
import featurea.runtime.*
import featurea.Application
import kotlinx.coroutines.runBlocking
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.lang.System as JvmSystem

@Provide(MainRenderProxy::class)
class MainRender(override val module: Module) : GLSurfaceView.Renderer, Component {

    private val app: Application = import()
    private val window: Window = import()

    private var isCreated: Boolean = false
    private var past: Long = -1

    override fun onCreateComponent() {
        provide(MainRenderProxy(this))
    }

    override fun onSurfaceCreated(skip: GL10, config: EGLConfig) {
        if (!isCreated) {
            isCreated = true
            window.init()
            app.onCreate()
        }
    }

    override fun onSurfaceChanged(skip: GL10, width: Int, height: Int) {
        window.resize(width, height)
    }

    override fun onDrawFrame(skip: GL10) {
        val now = JvmSystem.nanoTime()
        if (past == -1L) {
            past = now
        }
        val elapsedTime: Float = (now - past) / 1_000_000f
        past = now
        runBlocking {
            try {
                window.update(elapsedTime)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}
