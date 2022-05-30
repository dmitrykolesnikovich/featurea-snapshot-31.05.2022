package featurea.studio.project.menu

import featurea.desktop.runOnJfxThread
import featurea.math.Point
import featurea.math.Size
import featurea.math.toPoint
import featurea.math.toSize
import featurea.rml.RmlResource
import featurea.content.ResourceTag
import featurea.rml.findAttribute
import featurea.utils.runOnApplicationThread
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.editor.components.SelectionService
import featurea.studio.editor.components.isMovable
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class AlignMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Edit", "Align", "Align Left").apply {
                onAction = EventHandler { project.findSelectedEditorOrNull()?.alignLeft() }
                accelerator = KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Align", "Align Top").apply {
                onAction = EventHandler { project.findSelectedEditorOrNull()?.alignTop() }
                accelerator = KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Align", "Align Right").apply {
                onAction = EventHandler { project.findSelectedEditorOrNull()?.alignRight() }
                accelerator = KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Align", "Align Bottom").apply {
                onAction = EventHandler { project.findSelectedEditorOrNull()?.alignBottom() }
                accelerator = KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Align", "Align Vertically").apply {
                onAction = EventHandler { project.findSelectedEditorOrNull()?.alignVertically() }
                accelerator = KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Align", "Align Horizontally").apply {
                onAction = EventHandler { project.findSelectedEditorOrNull()?.alignHorizontally() }
                accelerator = KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
        }
    }

}

/*internals*/

private val ResourceTag.positionAttribute: Point
    get() = (findAttribute("position") ?: "0, 0").toPoint() // by design default (0, 0)

private val ResourceTag.sizeAttribute: Size
    get() = (findAttribute("size") ?: "0, 0").toSize() // by design default (0, 0)

private suspend fun Editor.alignValue(rmlResource: RmlResource, rmlTag: ResourceTag, value: String) {
    with(rmlResource) {
        val filteredValue = filterAttribute(rmlTag, "position", value)
        rmlTag.buildAttribute("position", filteredValue)
    }
}

private fun Editor.align(action: suspend (rmlTags: List<ResourceTag>) -> Unit) = runOnApplicationThread {
    val selectionService: SelectionService = import()

    val rmlTags = selectionService.selectedRmlTags.filter { it.isMovable(this) }
    action(rmlTags)
    tab.isDirty = true
}

private fun Editor.alignLeft() = align { rmlTags ->
    val minX = rmlTags.map { it.positionAttribute.x }.minBy { it } ?: return@align
    for (rmlTag in rmlTags) {
        val position = rmlTag.positionAttribute
        alignValue(tab.rmlResource, rmlTag, "${minX}, ${position.y}")
    }
}

private fun Editor.alignTop() = align { rmlTags ->
    val minY = rmlTags.map { it.positionAttribute.y }.minBy { it } ?: return@align
    for (rmlTag in rmlTags) {
        val position = rmlTag.positionAttribute
        alignValue(tab.rmlResource, rmlTag, "${position.x}, ${minY}")
    }
}

private fun Editor.alignRight() = align { rmlTags ->
    val maxX = rmlTags.map { it.positionAttribute.x + it.sizeAttribute.width }.maxBy { it } ?: return@align
    for (rmlTag in rmlTags) {
        val position = rmlTag.positionAttribute
        val size = rmlTag.sizeAttribute
        alignValue(tab.rmlResource, rmlTag, "${maxX - size.width}, ${position.y}")
    }
}

private fun Editor.alignBottom() = align { rmlTags ->
    val maxY = rmlTags.map { it.positionAttribute.y + it.sizeAttribute.height }.maxBy { it } ?: return@align
    for (rmlTag in rmlTags) {
        val position = rmlTag.positionAttribute
        val size = rmlTag.sizeAttribute
        alignValue(tab.rmlResource, rmlTag, "${position.x}, ${maxY - size.height}")
    }
}

private fun Editor.alignVertically() = align { rmlTags ->
    val minX = rmlTags.map { it.positionAttribute.x }.minBy { it } ?: return@align
    val maxX = rmlTags.map { it.positionAttribute.x + it.sizeAttribute.width }.maxBy { it } ?: return@align
    val middleX = (minX + maxX) / 2
    for (rmlTag in rmlTags) {
        val position = rmlTag.positionAttribute
        val size = rmlTag.sizeAttribute
        alignValue(tab.rmlResource, rmlTag, "${middleX - size.width / 2}, ${position.y}")
    }
}

private fun Editor.alignHorizontally() = align { rmlTags ->
    val minY = rmlTags.map { it.positionAttribute.y }.minBy { it } ?: return@align
    val maxY = rmlTags.map { it.positionAttribute.y + it.sizeAttribute.height }.maxBy { it } ?: return@align
    val middleY = (minY + maxY) / 2
    for (rmlTag in rmlTags) {
        val position = rmlTag.positionAttribute
        val size = rmlTag.sizeAttribute
        alignValue(tab.rmlResource, rmlTag, "${position.x}, ${middleY - size.height / 2}")
    }
}
