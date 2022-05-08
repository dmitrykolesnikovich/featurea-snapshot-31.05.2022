package featurea.studio.editor.features

import featurea.app.Application
import featurea.desktop.MainPanelProxy
import featurea.desktop.jfx.onMouseEvent
import featurea.math.Size
import featurea.math.Vector2
import featurea.runtime.Component
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.editor.components.SelectionService
import featurea.studio.editor.components.isMovable
import featurea.window.Window
import featurea.window.toLocalCoordinates
import featurea.window.toLocalDimensions
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import javax.swing.JPanel

fun Component.moveSelection() {
    val editor: Editor = import()
    val app: Application = import()
    val mainPanel: JPanel = import(MainPanelProxy)
    val selectionResizeFeature: SelectionResizeFeature = import()
    val selectionService: SelectionService = import()
    val selectRegionFeature: SelectRegionEditorFeature = import()
    val window: Window = import()
    val utilitySize: Size.Result = Size().Result()
    val utilityVector: Vector2.Result = Vector2().Result()

    var lastX: Int? = null
    var lastY: Int? = null
    var isEnable: Boolean = true
    var isFirst: Boolean = true

    fun resetDrag() {
        lastX = null
        lastY = null
        isEnable = true
        isFirst = true
    }

    mainPanel.onMouseEvent(object : MouseAdapter() {
        override fun mouseDragged(event: MouseEvent) {
            // filter
            if (app.isEnable) return
            if (app.frameCount == 0L) return
            if (selectionResizeFeature.isActive) return
            if (event.button != BUTTON1) return
            if (selectionService.selection == null) return
            if (selectRegionFeature.isEnable) return

            // action
            if (isFirst) {
                isFirst = false
                val (x, y) = window.toLocalCoordinates(editor.delegate.camera, event.x.toFloat(), event.y.toFloat(), utilityVector)
                isEnable = selectionService.selections.any { it.isMovable() && it.rectangle.contains(x, y) }
            }
            if (isEnable) {
                val globalDx: Int = event.x - (lastX ?: event.x)
                val globalDy: Int = event.y - (lastY ?: event.y)
                val (dx, dy) =  window.toLocalDimensions(editor.delegate.camera, globalDx.toFloat(), globalDy.toFloat(), utilitySize)
                selectionService.moveSelections(dx, dy, true) {
                    window.invalidate()
                }
                lastX = event.x
                lastY = event.y
            }
        }

        override fun mouseReleased(event: MouseEvent) {
            if (app.frameCount == 0L) return
            resetDrag()
        }

        override fun mouseExited(event: MouseEvent) {
            if (app.frameCount == 0L) return
            resetDrag()
        }

    })
}
