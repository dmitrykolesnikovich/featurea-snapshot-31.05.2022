package featurea.studio.editor.components

import featurea.config.Config
import featurea.content.ResourceTag
import featurea.contentNotEquals
import featurea.desktop.jfx.onChange
import featurea.desktop.jfx.toImageView
import featurea.desktop.runOnJfxThread
import featurea.rml.RmlResource
import featurea.rml.selectionPathRelativeTo
import featurea.runtime.*
import featurea.studio.editor.Editor
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView

typealias RmlTagItemTreeMap = Map<ResourceTag, TreeItem<ResourceTag>>

class RmlTreeView(override val module: Module) : Component, TreeView<ResourceTag>() {

    private val editor: Editor = import()
    private val editorTab: EditorTab = import()
    private val selectionService: SelectionService = import()

    private val iconsConfig = Config("icons")
    private val itemMap = mutableMapOf<ResourceTag, TreeItem<ResourceTag>>()
    val selectedRmlTagUI: ResourceTag? get() = selectionModel.selectedItem?.value
    private val Selection.item: TreeItem<ResourceTag>? get() = itemMap[rmlTag]

    override fun onCreateComponent() {
        cellFactory = RmlTreeViewCellFactory(editor)
        selectionModel.selectionMode = SelectionMode.MULTIPLE
        selectionService.selections.onChange {
            if (editor.isSkipUpdateSelectionUi) return@onChange

            val oldItems = selectionModel.selectedItems
            val newItems = selectionService.selections.mapNotNull { it.item }
            if (oldItems.contentNotEquals(newItems)) {
                runOnJfxThread {
                    editor.skipUpdateSelectionModel {
                        selectionModel.clearSelection()
                        for (newItem in newItems) {
                            selectionModel.select(newItem)
                        }
                    }
                }
            }
        }

        editorTab.rmlResourceProperty.watch {
            updateRmlTag()
        }
    }

    fun select(rmlTag: ResourceTag) = runOnJfxThread {
        selectionModel.clearSelection() // just for debug todo delete this
        val treeItem: TreeItem<ResourceTag> = itemMap[rmlTag] ?: return@runOnJfxThread
        treeItem.isExpanded = true
        selectionModel.select(treeItem)
        scrollTo(selectionModel.selectedIndex)
    }

    fun selectFirst(predicate: (rmlTag: ResourceTag) -> Boolean) = runOnJfxThread {
        for ((rmlTag, item) in itemMap) {
            if (predicate(rmlTag)) {
                selectionModel.select(item)
            }
        }
    }

    fun updateRmlTag() = runOnJfxThread {
        editor.skipUpdateSelectionModel {
            restoreSelections(editorTab.rmlResource, itemMap) {
                itemMap.clear()
                rootProperty().value = createRmlTagTreeItemRecursively(editorTab.rmlResource.rmlTag)
                root.isExpanded = true
            }
        }
    }

    /*internals*/

    private fun createRmlTagTreeItemRecursively(rmlTag: ResourceTag): TreeItem<ResourceTag> {
        val iconPath = iconsConfig[rmlTag.name]
        val item = if (iconPath == null) TreeItem(rmlTag) else TreeItem(rmlTag, iconPath.toImageView(16))
        itemMap[rmlTag] = item
        for (childRmlTag in rmlTag.children) {
            item.children.add(createRmlTagTreeItemRecursively(childRmlTag))
        }
        return item
    }

}

/*convenience*/

val TreeView<ResourceTag>.selectedRmlTagsUI: List<ResourceTag>
    get() = selectionModel.selectedItems.mapNotNull { it?.value }

inline fun TreeView<ResourceTag>.restoreSelections(
    rmlResource: RmlResource,
    itemMap: RmlTagItemTreeMap,
    action: () -> Unit
) {
    val oldSelectionPaths = selectedRmlTagsUI.map { rmlTag ->
        rmlTag.selectionPathRelativeTo(rmlResource.rmlTag)
    }
    action()
    for ((rmlTag, item) in itemMap) {
        val newSelectionPath = rmlTag.selectionPathRelativeTo(rmlResource.rmlTag)
        for (oldSelectionPath in oldSelectionPaths) {
            if (newSelectionPath.contentEquals(oldSelectionPath)) {
                selectionModel.select(item)
            }
        }
    }
}
