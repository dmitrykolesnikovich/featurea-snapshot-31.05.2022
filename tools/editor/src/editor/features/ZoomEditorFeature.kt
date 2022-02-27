package featurea.studio.editor.features

import featurea.Application
import featurea.DefaultApplicationDelegate
import featurea.breakpoint
import featurea.desktop.MainPanelProxy
import featurea.desktop.jfx.isShiftKeyPressed
import featurea.desktop.jfx.isWheelButton
import featurea.runOnUpdateOnJfxThread
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.window.Window
import featurea.window.notifyResize
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.pow

class ZoomEditorFeature(module: Module) : EditorFeature(module) {

    private val app: Application = import()
    private val mainPanel: JPanel = import(MainPanelProxy)
    private val window: Window = import()

    private var lastX: Int? = null
    private var lastY: Int? = null

    init {
        mainPanel.addMouseWheelListener { event ->
            /*if (app.delegate is EditorModuleDelegate) return@addMouseWheelListener*/
            if (window.useCamera) return@addMouseWheelListener

            app.runOnUpdateOnJfxThread {
                var step = 1.05f
                if (isShiftKeyPressed) {
                    step = step.toDouble().pow(6.0).toFloat()
                }
                val count: Float = event.wheelRotation.toFloat()
                val x = event.x.toFloat()
                val y = event.y.toFloat()
                var ratio = if (count > 0) 1 / step else step
                ratio = ratio.toDouble().pow(abs(count).toDouble()).toFloat()
                window.surface.transform.edit {
                    scale(x, y, ratio)
                }
                window.notifyResize() // quickfix todo conceptualize
                window.invalidate()
            }
        }
        mainPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(event: MouseEvent) {
                if (app.frameCount == 0L) return
                if (window.useCamera) return

                if (event.button.isWheelButton) {
                    val dx = (event.x - (lastX ?: event.x)) / window.surface.transform.sx
                    val dy = (event.y - (lastY ?: event.y)) / window.surface.transform.sy
                    app.runOnUpdateOnJfxThread {
                        window.surface.transform.edit {
                            translate(dx, dy)
                        }
                        window.notifyResize() // quickfix todo conceptualize
                        window.invalidate()
                    }
                    lastX = event.x
                    lastY = event.y
                }
            }
        })
        mainPanel.addMouseListener(object : MouseAdapter() {

            override fun mouseReleased(event: MouseEvent) {
                lastX = null
                lastY = null
            }

            override fun mouseExited(event: MouseEvent) {
                lastX = null
                lastY = null
            }

        })
    }

}
