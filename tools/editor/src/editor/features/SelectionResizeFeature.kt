package featurea.studio.editor.features

import featurea.Application
import featurea.content.ResourceAttribute
import featurea.desktop.MainPanelProxy
import featurea.math.Point
import featurea.math.Size
import featurea.math.toPoint
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.editor.components.Selection
import featurea.studio.editor.components.positionKey
import featurea.studio.editor.components.sizeKey
import featurea.studio.editor.features.ResizedSide.*
import featurea.window.Window
import featurea.window.toLocalDimensions
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

class SelectionResizeFeature(override val module: Module) : Component, MouseAdapter() {

    private val app: Application = import()
    private val editor: Editor = import()
    private val mainPanel: JPanel = import(MainPanelProxy)
    private val selectionResizedSideFeature: SelectionResizeTrackFeature = import()
    private val window: Window = import()

    var isActive: Boolean = false
    private var lastX: Int? = null
    private var lastY: Int? = null
    private val sr: Size.Result = Size().Result()

    init {
        mainPanel.addMouseMotionListener(this)
        mainPanel.addMouseListener(this)
    }

    override fun mouseDragged(event: MouseEvent) {
        // filter
        if (app.frameCount == 0L) return
        if (selectionResizedSideFeature.resizedSide.isUnknown()) return
        if (app.isEnable) return

        // action
        isActive = true
        val dx = event.x - (lastX ?: event.x)
        val dy = event.y - (lastY ?: event.y)
        resizeSelection(dx.toFloat(), dy.toFloat())
        lastX = event.x
        lastY = event.y
    }

    override fun mousePressed(event: MouseEvent) {}
    override fun mouseReleased(event: MouseEvent) = deactivate()
    override fun mouseExited(event: MouseEvent) = deactivate()

    /*internals*/

    var prevSelection: Selection? = null

    private fun resizeSelection(_dx: Float, _dy: Float) {
        // filter
        val rmlTag = editor.selectionService.selectedRmlTag ?: return
        val (dx, dy) = window.toLocalDimensions(editor.delegate.camera, _dx, _dy, sr)
        if (dx == 0f && dy == 0f) return
        val selection = editor.selectionService.selection
        val region = selection?.localRectangle ?: return

        // action
        editor.updateEditorUi {
            if (prevSelection != selection) {
                prevSelection = selection
            }

            val positionKey = editor.selectionService.positionKey
            val sizeKey = editor.selectionService.sizeKey
            val (x1, y1) = (rmlTag.attributes[positionKey] ?: "0, 0").toPoint()
            val resultSize = Size()
            val resultPosition = Point()
            resultPosition.assign(x1, y1)
            resultSize.assign(region.width, region.height)
            when (selectionResizedSideFeature.resizedSide) {
                LEFT -> {
                    resultSize.assign(region.width - dx, region.height)
                    resultPosition.assign(x1 + dx, y1)
                }
                RIGHT -> {
                    resultSize.assign(region.width + dx, region.height)
                }
                TOP -> {
                    resultSize.assign(region.width, region.height - dy)
                    resultPosition.assign(x1, y1 + dy)
                }
                BOTTOM -> {
                    resultSize.assign(region.width, region.height + dy)
                }
                LEFT_TOP -> {
                    resultSize.assign(region.width - dx, region.height - dy)
                    resultPosition.assign(x1 + dx, y1 + dy)
                }
                RIGHT_TOP -> {
                    resultSize.assign(region.width + dx, region.height - dy)
                    resultPosition.assign(x1, y1 + dy)
                }
                RIGHT_BOTTOM -> {
                    resultSize.assign(region.width + dx, region.height + dy)
                }
                LEFT_BOTTOM -> {
                    resultSize.assign(region.width - dx, region.height + dy)
                    resultPosition.assign(x1 + dx, y1)
                }
                UNKNOWN -> {
                    // no op
                }
            }
            val position = ResourceAttribute(positionKey, "${resultPosition.x}, ${resultPosition.y}")
            val size = ResourceAttribute(sizeKey, "${resultSize.width}, ${resultSize.height}")
            editor.updateModel(rmlTag, position, size)
            window.invalidate()
        }
    }

    /*internals*/

    private fun deactivate() {
        isActive = false
        lastX = null
        lastY = null
    }

}
