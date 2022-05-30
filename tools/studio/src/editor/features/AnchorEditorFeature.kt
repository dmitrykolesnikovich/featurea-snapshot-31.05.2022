package featurea.studio.editor.features

import featurea.app.Application
import featurea.utils.Colors.redColor
import featurea.content.ResourceTag
import featurea.desktop.jfx.onChange
import featurea.desktop.runOnJfxThread
import featurea.graphics.Graphics
import featurea.math.*
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.studio.editor.components.*
import featurea.studio.project.Project
import featurea.studio.project.components.Palette
import featurea.utils.runOnApplicationThread
import featurea.window.Window
import featurea.window.toGlobalDimensions
import javafx.event.EventHandler
import kotlin.math.abs

private const val TOLERANCE: Int = 2

class AnchorEditorFeature(module: Module) : EditorFeature(module) {

    private val app: Application = import()
    private val mouse: MouseService = import()
    private val palette: Palette = import()
    private val project: Project = import()
    private val selectionService: SelectionService = import()
    private val window: Window = import()

    private val linesGraphics by lazy { Graphics { linesLimit = 4 } }
    private val anchors: Anchors = Anchors()
    private val isActive: Boolean get() = (mouse.isInsideEditor && palette.isActive) || mouse.isInsideEditorWithTouch
    private val utilitySize: Size.Result = Size().Result()
    private val outerBoundsResult: Rectangle = Rectangle()
    private val outerBounds: Rectangle
        get() = outerBoundsResult.apply {
            val rmlTag: ResourceTag = editor.tab.rmlResource.rmlTag
            val size: Size = (rmlTag.attributes["size"] ?: "0, 0").toSize()
            val position: Point = (rmlTag.attributes["position"] ?: "0, 0").toPoint()
            var x1: Float = position.x
            var y1: Float = position.y
            var x2: Float = x1 + size.width
            var y2: Float = y1 + size.height
            val instance: Any? = palette.instance
            if (instance != null) {
                val (left, top, right, bottom) = editor.delegate.immediateBoundsOf(instance)
                x1 = min(x1, left)
                y1 = min(y1, top)
                x2 = max(x2, right)
                y2 = max(y2, bottom)
            }
            assign(x1, y1, x2, y2)
        }

    init {
        runOnJfxThread {
            project.menuBar.findCheckMenuItem("View", "Alignment").apply {
                onAction = EventHandler {
                    isEnable = isSelected
                }
            }
        }
    }

    override fun onCreateComponent() {
        app.repeatOnUpdate {
            // filter
            if (editor.delegate.isHeadless) return@repeatOnUpdate
            if (!isEnable) return@repeatOnUpdate
            if (app.isEnable) return@repeatOnUpdate
            if (!isActive) return@repeatOnUpdate
            if (!linesGraphics.isValid) {
                for (anchorLine in anchors.toList()) {
                    linesGraphics.buffer { Line(anchorLine, redColor) }
                }
            }
            linesGraphics.draw(editor.delegate.camera)
        }
        selectionService.selections.onChange {
            reset()
        }
        mouse.onMove {
            reset()
        }
        window.repeatOnInvalidate {
            reset()
        }
    }

    override fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String {
        // filter
        if (editor.delegate.isHeadless) return value
        if (!isEnable) return value
        if (key != selectionService.positionKey) return value
        if (rmlTag.isNotMovable(editor)) return value

        // action
        reset()
        val position: Point = value.toPoint()
        val size = (rmlTag.attributes[selectionService.sizeKey] ?: "0, 0").toSize()

        val left: Line? = anchors.left
        val top: Line? = anchors.top
        val right: Line? = anchors.right
        val bottom: Line? = anchors.bottom
        if (left != null && deltaX(position.x, left.point1.x) < TOLERANCE) {
            position.x = left.point1.x
        } else if (right != null && deltaX(position.x, right.point1.x - size.width) < TOLERANCE) {
            position.x = right.point1.x - size.width
        }
        if (top != null && deltaX(position.y, top.point1.y) < TOLERANCE) {
            position.y = top.point1.y
        } else if (bottom != null && deltaX(position.y, bottom.point1.y - size.height) < TOLERANCE) {
            position.y = bottom.point1.y - size.height
        }
        return "${position.x}, ${position.y}"
    }

    /*internals*/

    private fun reset() = runOnApplicationThread {
        // filter
        if (!isActive) return@runOnApplicationThread
        if (editor.delegate.isHeadless) return@runOnApplicationThread

        // action
        linesGraphics.invalidate()
        anchors.reset()
    }

    private fun deltaX(from: Float, to: Float): Float {
        val (deltaX, _) = window.toGlobalDimensions(editor.delegate.camera, abs(from - to), 0f, utilitySize)
        return deltaX
    }

    private fun deltaY(from: Float, to: Float): Float {
        val (_, deltaY) = window.toGlobalDimensions(editor.delegate.camera, 0f, abs(from - to), utilitySize)
        return deltaY
    }

    inner class Anchors {

        var left: Line? = null
        var top: Line? = null
        var right: Line? = null
        var bottom: Line? = null
        private val lines = mutableListOf<Line>()

        fun reset() {
            left = null
            top = null
            right = null
            bottom = null

            val instance: Any? = palette.instance
            val immediateBounds: Rectangle? = if (instance != null) {
                editor.delegate.immediateBoundsOf(instance)
            } else {
                selectionService.selection?.rectangle
            }
            val (x1, y1, x2, y2) = immediateBounds ?: return
            var leftLine: Float? = null
            var topLine: Float? = null
            var rightLine: Float? = null
            var bottomLine: Float? = null
            val selections: List<Selection> = selectionService.allPossibleSelections
            val selectionsExceptCurrent: List<Selection> = when {
                palette.instance != null -> selections
                else -> selections.filter { it != selectionService.selection }
            }
            val selectionRectanglesExceptCurrent: List<Rectangle> = selectionsExceptCurrent.map { it.rectangle }
            for (rectangle in selectionRectanglesExceptCurrent) {
                if (leftLine == null) {
                    if (deltaX(x1, rectangle.left) < TOLERANCE) leftLine = rectangle.left
                    if (deltaX(x1, rectangle.right) < TOLERANCE) leftLine = rectangle.right
                }
                if (topLine == null) {
                    if (deltaY(y1, rectangle.top) < TOLERANCE) topLine = rectangle.top
                    if (deltaY(y1, rectangle.bottom) < TOLERANCE) topLine = rectangle.bottom
                }
                if (rightLine == null) {
                    if (deltaX(x2, rectangle.left) < TOLERANCE) rightLine = rectangle.left
                    if (deltaX(x2, rectangle.right) < TOLERANCE) rightLine = rectangle.right
                }
                if (bottomLine == null) {
                    if (deltaY(y2, rectangle.top) < TOLERANCE) bottomLine = rectangle.top
                    if (deltaY(y2, rectangle.bottom) < TOLERANCE) bottomLine = rectangle.bottom
                }
            }
            val (leftX, topY, rightX, bottomY) = outerBounds
            leftLine?.let { left = Line(Point(it, topY), Point(it, bottomY)) }
            topLine?.let { top = Line(Point(leftX, it), Point(rightX, it)) }
            rightLine?.let { right = Line(Point(it, topY), Point(it, bottomY)) }
            bottomLine?.let { bottom = Line(Point(leftX, it), Point(rightX, it)) }
        }

        fun toList(): List<Line> {
            lines.clear()
            left?.let { lines.add(it) }
            top?.let { lines.add(it) }
            right?.let { lines.add(it) }
            bottom?.let { lines.add(it) }
            return lines
        }

    }

}
