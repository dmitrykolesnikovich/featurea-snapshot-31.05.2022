package featurea.studio.editor.components

import featurea.content.ResourceAttribute
import featurea.content.ResourceTag
import featurea.desktop.jfx.onChange
import featurea.desktop.runOnJfxThread
import featurea.math.Rectangle
import featurea.math.toPoint
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.utils.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.runBlocking

class SelectionService(override val module: Module) : Component {

    val selections: ObservableList<Selection> = FXCollections.observableArrayList()
    val selection: Selection? get() = selections.lastOrNull()
    val selectedRmlTag: ResourceTag? get() = selection?.rmlTag
    val selectedRmlTags: List<ResourceTag> get() = selections.map { it.rmlTag }
    val selectedResourceAttributeProperty = Property<ResourceAttribute?>()
    var selectedResourceAttribute: ResourceAttribute? by selectedResourceAttributeProperty
    val selectedRmlKey: String? get() = selectedResourceAttribute?.key

    // import
    val editor: Editor = import()
    val rmlTableView: RmlTableView = import()
    val rmlTreeView: RmlTreeView = import()

    private val allPossibleSelectionsResult = mutableListOf<Selection>()
    val allPossibleSelections: List<Selection>
        get() {
            allPossibleSelectionsResult.clear()
            val rootRmlTag: ResourceTag = editor.tab.rmlResource.rmlTag
            val rootSelection: Selection? = editor.delegate.selectionOf(rootRmlTag)
            val childrenSelections: List<Selection> = rootRmlTag.children.mapNotNull { editor.delegate.selectionOf(it) }
            if (rootSelection != null) {
                allPossibleSelectionsResult.add(rootSelection)
            }
            allPossibleSelectionsResult.addAll(childrenSelections)
            return allPossibleSelectionsResult
        }

    init {
        rmlTreeView.selectionModel.selectedItems.onChange {
            if (editor.isSkipUpdateSelectionModel) return@onChange

            val rmlTreeViewSelections = rmlTreeView.selectedRmlTagsUI.map { with(editor.delegate) { selectionOf(it) } }
            runOnJfxThread {
                editor.skipUpdateSelectionUI {
                    // remove
                    val removed = selections.filter { !rmlTreeViewSelections.contains(it) }
                    selections.removeAll(removed)

                    // append
                    val appended = rmlTreeViewSelections.filter { !selections.contains(it) }
                    selections.addAll(appended)
                }
            }
        }
    }

    fun select(vararg rmlTags: ResourceTag) {
        selections.setAll(rmlTags.map { with(editor.delegate) { selectionOf(it) } })
    }

    fun selectAll() {
        select(*editor.tab.rmlResource.rmlTag.children.toTypedArray())
    }

    fun select(localRectangle: Rectangle, predicate: (Selection) -> Boolean) {
        val movableSelections = editor.delegate.select(localRectangle).filter(predicate)
        if (movableSelections.isNotEmpty()) {
            selections.setAll(movableSelections)
        }
    }

    fun clearSelectedValue() = runBlocking {
        val selectedRmlTag = rmlTreeView.selectedRmlTagUI ?: return@runBlocking
        val attribute = rmlTableView.selectionModel.selectedItem ?: return@runBlocking
        with(editor.tab.rmlResource) {
            selectedRmlTag.removeAttribute(attribute.key, attribute.value)
            selectedRmlTag.build()
        }
        editor.tab.isDirty = true
        // >> quickfix todo improve
        rmlTreeView.refresh()
        rmlTableView.updateRmlTag()
        // <<
    }

    fun moveSelections(dx1: Float, dy1: Float, isFilterEnable: Boolean, onFinish: () -> Unit = {}) {
        // filter
        if (dx1 == 0f && dy1 == 0f) return
        val selection: Selection = selection ?: return

        // action
        val movableSelections: List<Selection> = selections.filter { it.isMovable() }
        editor.updateEditorUi {
            val (dx, dy) = selection.move(dx1, dy1, isFilterEnable)
            val dx2 = if (dx == 0f) 0f else dx1
            val dy2 = if (dy == 0f) 0f else dy1
            for (movableSelection in movableSelections.filter { it != selection }) {
                movableSelection.move(dx2, dy2, isFilterEnable)
            }
            onFinish()
        }
    }

    /*internals*/

    private suspend fun Selection.move(dx: Float, dy: Float, isFilterEnable: Boolean): Pair<Float, Float> {
        val (x1, y1) = (rmlTag.attributes[positionKey] ?: "0, 0").toPoint()
        val position: ResourceAttribute = ResourceAttribute(positionKey, "${x1 + dx}, ${y1 + dy}")
        editor.updateModel(rmlTag, position, isFilterEnable = isFilterEnable)
        val (x2, y2) = (rmlTag.attributes[positionKey] ?: "0, 0").toPoint()
        return x2 - x1 to y2 - y1
    }

}

val SelectionService.positionKey: String
    get() {
        val selectedKey = selectedRmlKey
        if (selectedKey != null && selectedKey.endsWith("Position")) return selectedKey
        if (selectedKey != null && selectedKey.endsWith("Size")) return selectedKey.replace("Size", "Position")
        return "position"
    }

val SelectionService.sizeKey: String
    get() {
        val selectedKey = selectedRmlKey
        if (selectedKey != null && selectedKey.endsWith("Size")) return selectedKey
        if (selectedKey != null && selectedKey.endsWith("Position")) return selectedKey.replace("Position", "Size")
        return "size"
    }
