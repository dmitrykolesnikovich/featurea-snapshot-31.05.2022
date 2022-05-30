package featurea.studio.editor.features

import featurea.app.Application
import featurea.desktop.MainPanelProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.editor.features.ResizedSide.*
import featurea.studio.project.components.Palette
import java.awt.Cursor
import java.awt.Cursor.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

class SelectionResizeTrackFeature(override val module: Module) : Component, MouseAdapter() {

    private val app: Application = import()
    private val editor: Editor = import()
    private val mainPanel: JPanel = import(MainPanelProxy)
    private val palette: Palette = import()
    private val selectionResizeFeature: SelectionResizeFeature = import()

    var resizedSide: ResizedSide = UNKNOWN
        private set

    init {
        mainPanel.addMouseMotionListener(this)
    }

    override fun mouseMoved(event: MouseEvent) {
        // filter
        if (app.frameCount == 0L) return
        if (editor.delegate.isHeadless) return
        if (selectionResizeFeature.isActive) return
        if (palette.isActive) return
        if (app.isEnable) return

        val (_x1, _y1, _x2, _y2) = editor.selectionService.selection?.globalRectangle ?: return
        val x = event.x
        val y = event.y

        val left = _x1 - tolerance < x && x < _x1 + tolerance && _y1 - tolerance < y && y < _y2 + tolerance
        val right = _x2 - tolerance < x && x < _x2 + tolerance && _y1 - tolerance < y && y < _y2 + tolerance
        val top = _y1 - tolerance < y && y < _y1 + tolerance && _x1 - tolerance < x && x < _x2 + tolerance
        val bottom = _y2 - tolerance < y && y < _y2 + tolerance && _x1 - tolerance < x && x < _x2 + tolerance
        val leftTop = left && top
        val rightTop = right && top
        val rightBottom = right && bottom
        val leftBottom = left && bottom

        resizedSide = when {
            leftTop -> LEFT_TOP
            rightTop -> RIGHT_TOP
            rightBottom -> RIGHT_BOTTOM
            leftBottom -> LEFT_BOTTOM
            left -> LEFT
            top -> TOP
            right -> RIGHT
            bottom -> BOTTOM
            else -> UNKNOWN
        }

        when (resizedSide) {
            LEFT -> mainPanel.cursor = Cursor(W_RESIZE_CURSOR)
            RIGHT -> mainPanel.cursor = Cursor(E_RESIZE_CURSOR)
            TOP -> mainPanel.cursor = Cursor(N_RESIZE_CURSOR)
            BOTTOM -> mainPanel.cursor = Cursor(S_RESIZE_CURSOR)
            LEFT_TOP -> mainPanel.cursor = Cursor(NW_RESIZE_CURSOR)
            RIGHT_TOP -> mainPanel.cursor = Cursor(NE_RESIZE_CURSOR)
            RIGHT_BOTTOM -> mainPanel.cursor = Cursor(SE_RESIZE_CURSOR)
            LEFT_BOTTOM -> mainPanel.cursor = Cursor(SW_RESIZE_CURSOR)
            UNKNOWN -> mainPanel.cursor = Cursor(DEFAULT_CURSOR)
        }
    }

}

/*internals*/

private const val tolerance: Int = 4

enum class ResizedSide {
    UNKNOWN,
    LEFT,
    TOP,
    RIGHT,
    BOTTOM,
    LEFT_TOP,
    RIGHT_TOP,
    RIGHT_BOTTOM,
    LEFT_BOTTOM;

    fun isKnown() = this != UNKNOWN
    fun isUnknown() = this == UNKNOWN
}
