package featurea.studio.editor.components

import featurea.content.ResourceTag
import featurea.utils.log
import featurea.studio.editor.Editor
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.input.*
import javafx.util.Callback
import javafx.util.StringConverter
import java.util.*

private typealias RmlTreeViewCell = TextFieldTreeCell<ResourceTag>

private const val DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 0 0 2 0; -fx-padding: 3 3 1 3"

class RmlTreeViewCellFactory(val editor: Editor) : Callback<TreeView<ResourceTag>, TreeCell<ResourceTag>> {

    private var dropZone: TextFieldTreeCell<ResourceTag>? = null
    private val draggedItems = mutableListOf<TreeItem<ResourceTag>>()

    override fun call(param: TreeView<ResourceTag>?): TreeCell<ResourceTag> {
        return RmlTreeViewCell().apply {
            converter = object : StringConverter<ResourceTag>() {
                override fun toString(rmlTag: ResourceTag): String {
                    return rmlTag.attributes["name"] ?: rmlTag.name
                }

                override fun fromString(name: String): ResourceTag {
                    throw UnsupportedOperationException("fromString")
                }
            }
            setOnDragDetected { event -> dragDetected(event, this) }
            setOnDragOver { event -> dragOver(event, this) }
            setOnDragDropped { event -> drop(event, this) }
            setOnDragDone { clearDropLocation() }
        }
    }

    private fun dragDetected(event: MouseEvent, cell: RmlTreeViewCell) {
        // filter
        if (cell.treeItem?.parent == null) return

        // action
        val dragboard: Dragboard = cell.startDragAndDrop(TransferMode.MOVE)
        dragboard.apply {
            val content = ClipboardContent()
            draggedItems.clear()
            draggedItems.addAll(cell.treeView.selectionModel.selectedItems)
            draggedItems.sortBy { it.parent.children.indexOf(it) }
            log("detected: ${draggedItems.joinToString { it.value.name }}")
            content.putString("tags")
            setContent(content)
            dragView = cell.snapshot(null, null)
        }
        event.consume()
    }

    private fun dragOver(event: DragEvent, cell: RmlTreeViewCell) {
        event.acceptTransferModes(TransferMode.MOVE)
        if (!Objects.equals(dropZone, cell)) {
            clearDropLocation()
            dropZone = cell
            dropZone?.style = DROP_HINT_STYLE
        }
    }

    private fun drop(event: DragEvent, cell: RmlTreeViewCell) {
        // filter
        if (draggedItems.isEmpty()) return
        val dropItem = cell.treeItem ?: return

        val parent = draggedItems.first().parent
        draggedItems.reverse()

        for (draggedItem in draggedItems) {
            val rmlTag = draggedItem.value

            // 1. remove from previous location
            parent.children.remove(draggedItem)

            // 2. add to new location
            val index = if (Objects.equals(parent, dropItem)) 0 else parent.children.indexOf(dropItem) + 1
            if (index >= parent.children.size) {
                parent.children.add(draggedItem)
            } else {
                parent.children.add(index, draggedItem)
            }
            editor.replace(rmlTag, index) // just for try todo delete this
        }

        event.isDropCompleted = false
    }

    private fun clearDropLocation() {
        val dropZone = dropZone ?: return
        dropZone.style = ""
    }

}
