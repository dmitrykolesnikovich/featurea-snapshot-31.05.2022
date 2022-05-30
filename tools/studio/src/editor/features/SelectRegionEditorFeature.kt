package featurea.studio.editor.features

import featurea.app.Application
import featurea.utils.Colors
import featurea.desktop.MainPanelProxy
import featurea.desktop.jfx.isShortcutKeyPressed
import featurea.desktop.jfx.onMouseEvent
import featurea.graphics.Graphics
import featurea.math.Rectangle
import featurea.math.Vector2
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.studio.editor.components.Selection
import featurea.studio.editor.components.isMovable
import featurea.studio.editor.components.isNotMovable
import featurea.studio.project.components.Palette
import featurea.window.Window
import featurea.window.toLocalCoordinates
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import javax.swing.JPanel

class SelectRegionEditorFeature(module: Module) : EditorFeature(module) {

    private val app: Application = import()
    private val mainPanel: JPanel = import(MainPanelProxy)
    private val palette: Palette = import()
    private val selectionResizeTrackFeature: SelectionResizeTrackFeature = import()
    private val window: Window = import()

    private val rectangleGraphics by lazy { Graphics { rectanglesLimit = 1 } }
    private var rectangle: Rectangle? = null
    private val utilityVector: Vector2.Result = Vector2().Result()
    var isEnableQuickfix: Boolean = true

    init {
        window.repeatOnInvalidate {
            if (editor.delegate.isHeadless) return@repeatOnInvalidate
            rectangleGraphics.invalidate()
        }

        app.repeatOnUpdate {
            if (editor.delegate.isHeadless) return@repeatOnUpdate
            if (app.isEnable) return@repeatOnUpdate
            val rectangle = rectangle ?: return@repeatOnUpdate

            if (!rectangleGraphics.isValid) {
                rectangleGraphics.buffer {
                    Rectangle(rectangle, Colors.blueColor)
                }
            }
            rectangleGraphics.draw(editor.delegate.camera)
        }

        mainPanel.onMouseEvent(object : MouseAdapter() {

            private var isFirst: Boolean = true

            override fun mousePressed(event: MouseEvent) {
                /*mainPanel.parent.requestFocus() // just for debug todo delete this*/

                // filter
                if (app.frameCount == 0L) return
                if (editor.delegate.isHeadless) return
                if (palette.isActive) return
                if (selectionResizeTrackFeature.resizedSide.isKnown()) return
                if (event.button != BUTTON1) return

                // action
                val x: Float = event.x.toFloat()
                val y: Float = event.y.toFloat()
                val selection: Selection? = editor.delegate.select(x, y)
                if (selection != null) {
                    if (isShortcutKeyPressed) {
                        if (editor.selectionService.selections.contains(selection)) {
                            editor.selectionService.selections.remove(selection)
                        } else {
                            editor.selectionService.selections.add(selection)
                        }
                    } else {
                        if (editor.selectionService.selections.contains(selection)) {
                            // IMPORTANT here I change last element in selections list
                            editor.skipUpdateSelectionUI {
                                editor.selectionService.selections.remove(selection)
                                editor.selectionService.selections.add(selection)
                            }
                        } else {
                            editor.selectionService.selections.setAll(selection)
                        }
                    }
                } else if (editor.selectionService.selections.size >= 2) {
                    editor.selectionService.selections.setAll()
                }
            }

            override fun mouseDragged(event: MouseEvent) {
                // filter
                if (!isEnableQuickfix) return
                if (app.frameCount == 0L) return
                if (editor.delegate.isHeadless) return
                if (palette.isActive) return
                if (selectionResizeTrackFeature.resizedSide.isKnown()) return
                if (event.button != BUTTON1) return

                // action
                if (isFirst) {
                    isFirst = false
                    isEnable = editor.delegate.select(event.x.toFloat(), event.y.toFloat())?.isNotMovable() ?: true
                }
                if (isEnable) {
                    val (x, y) = window.toLocalCoordinates(editor.delegate.camera, event.x.toFloat(), event.y.toFloat(), utilityVector)
                    if (rectangle === null) {
                        rectangle = Rectangle(x, y, x, y)
                    } else {
                        rectangle?.apply {
                            x2 = x
                            y2 = y
                            editor.selectionService.select(normalizedRectangle()) { it.isMovable() }
                        }
                    }
                    rectangleGraphics.invalidate()
                }
            }

            override fun mouseReleased(event: MouseEvent) = resetDrag()

            override fun mouseExited(event: MouseEvent) = resetDrag()

            private fun resetDrag() {
                rectangle = null
                isEnable = true
                isFirst = true
            }

        })
    }
}
