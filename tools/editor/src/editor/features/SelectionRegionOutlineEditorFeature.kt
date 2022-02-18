package featurea.studio.editor.features

import featurea.Application
import featurea.Colors.blackColor
import featurea.Colors.cyanColor
import featurea.desktop.jfx.onChange
import featurea.graphics.Graphics
import featurea.loader.Loader
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.studio.editor.components.Selection
import featurea.window.Window

class SelectionRegionOutlineEditorFeature(module: Module) : EditorFeature(module) {

    private val app: Application = import()
    private val window: Window = import()

    private val selectionsGraphics by lazy { Graphics() }
    private val rectanglesGraphics by lazy { Graphics() }

    /*
    init {
        runOnJfxThread {
            projectMenuBar.findMenu("View").items.add(CheckMenuItem("Outline").apply {
                isSelected = true
                onAction = EventHandler {
                    isEnable = isSelected
                    window.invalidate()
                }
            })
        }
    }
    */

    override fun onCreateComponent() {
        app.repeatOnUpdate {
            // filter
            if (editor.delegate.isHeadless) return@repeatOnUpdate
            if (app.isEnable) return@repeatOnUpdate
            if (window.useCamera) return@repeatOnUpdate

            // rectangles
            if (isEnable) {
                val selections: List<Selection> = editor.selectionService.allPossibleSelections
                val rectanglesSize: Int = editor.tab.rmlResource.rmlTag.children.size + 1
                rectanglesGraphics.invalidate()
                rectanglesGraphics.rectangles.increaseDrawCallLimit(rectanglesSize)
                if (!rectanglesGraphics.isValid) {
                    for (selection in selections) {
                        val (x1, y1, x2, y2) = selection.rectangle
                        rectanglesGraphics.buffer {
                            Rectangle(x1, y1, x2, y2, blackColor)
                        }
                    }
                }
                rectanglesGraphics.draw(editor.delegate.camera)
            }

            // selection rectangle
            selectionsGraphics.invalidate()
            selectionsGraphics.rectangles.increaseDrawCallLimit(editor.selectionService.selectedRmlTags.size)
            if (!selectionsGraphics.isValid) {
                for (rmlTag in ArrayList(editor.selectionService.selectedRmlTags)) {
                    val selection: Selection? = with(editor.delegate) { selectionOf(rmlTag) }
                    if (selection != null) {
                        val (x1, y1, x2, y2) = selection.rectangle
                        selectionsGraphics.buffer {
                            Rectangle(x1, y1, x2, y2, cyanColor)
                        }
                    }
                }
            }
            selectionsGraphics.draw(editor.delegate.camera)
        }
        window.repeatOnInvalidate {
            if (editor.delegate.isHeadless) return@repeatOnInvalidate
            selectionsGraphics.invalidate()
            rectanglesGraphics.invalidate()
        }
        editor.selectionService.selections.onChange {
            if (editor.delegate.isHeadless) return@onChange
            selectionsGraphics.invalidate()
            rectanglesGraphics.invalidate()
        }
    }
}
