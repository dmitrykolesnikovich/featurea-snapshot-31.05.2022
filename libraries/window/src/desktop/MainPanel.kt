package featurea.window

import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.util.FPSAnimator
import featurea.*
import featurea.desktop.MainNodeProxy
import featurea.desktop.MainPanelProxy
import featurea.desktop.SwingNode
import featurea.desktop.jogamp.DefaultGLCapabilities
import featurea.math.Size
import featurea.runtime.*
import featurea.utils.Color
import featurea.utils.toColor
import kotlinx.coroutines.runBlocking
import java.lang.System as JvmSystem

val MainPanelColor: Color = "#F6F6F6FF".toColor()

@Provide(MainPanelProxy::class)
@Provide(MainNodeProxy::class)
class MainPanel(override val module: Module) : Component, GLJPanel(DefaultGLCapabilities()), GLEventListener {

    private val app: Application = import()
    private val mainPanel: MainPanel = import()
    private val system: System = import()
    private val window: Window = import()

    private var isCreated: Boolean = false
    private var past: Long = -1L
    private var currentWidth: Int = 0
    private var currentHeight: Int = 0

    init {
        addGLEventListener(this)
    }

    override fun onCreateComponent() {
        provide(MainPanelProxy(mainPanel))
        provide(MainNodeProxy(SwingNode(content = mainPanel)))
        animator = FPSAnimator(system.properties["fps"] ?: 60)
        animator.add(this)
        animator.start()
    }

    override fun onDeleteComponent() {
        animator.stop()
    }

    override fun init(drawable: GLAutoDrawable?) = runBlocking {
        window.init()
        gl.clearWithMainPanelColorQuickfix()
    }

    override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {
        currentWidth = width
        currentHeight = height
        // log("[MainPanel] reshape: $width, $height")
    }

    override fun display(drawable: GLAutoDrawable?) = runBlocking {
        val now: Long = JvmSystem.nanoTime()
        if (past == -1L) {
            past = now
        }
        val elapsedTime: Float = (now - past) / 1_000_000f
        past = now
        if (!isCreated) {
            try {
                app.onCreate()
                app.onStart()
            } finally {
                isCreated = true
            }
        }
        try {
            gl.clearWithMainPanelColorQuickfix()
            val size: Size = window.surface.size
            if (size.width != currentWidth.toFloat() || size.height != currentHeight.toFloat()) {
                window.resize(currentWidth, currentHeight)
            }
            window.update(elapsedTime)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun dispose(drawable: GLAutoDrawable?) {
        app.onStop()
        app.onDestroy()
    }

}
