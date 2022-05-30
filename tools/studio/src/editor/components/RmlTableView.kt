package featurea.studio.editor.components

import featurea.config.Config
import featurea.content.ResourceAttribute
import featurea.content.ResourceTag
import featurea.desktop.jfx.FSTableView
import featurea.desktop.jfx.onChange
import featurea.desktop.jfx.requestResize
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.editor.findAttributeKeys
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.TableColumn
import javafx.util.Callback

typealias ResourceAttributeCell = TableColumn.CellDataFeatures<ResourceAttribute, String>

class RmlTableView(override val module: Module) : Component, FSTableView<ResourceAttribute>() {

    private val editor: Editor = import()
    private val rmlTableView: RmlTableView = import()
    private val rmlTreeView: RmlTreeView = import()
    private val selectionService: SelectionService = import()
    var currentRmlTag: ResourceTag? = null
        private set
    val externalEditors = Config("externalEditors")
    val enums = Config("enums")
    private val keysResult = mutableListOf<String>()

    override fun onCreateComponent() {
        columns.add(TableColumn<ResourceAttribute, String>("key").apply {
            isEditable = false
            cellValueFactory = Callback<ResourceAttributeCell, ObservableValue<String>> {
                val attribute: ResourceAttribute = it.value
                ReadOnlyStringWrapper(attribute.key)
            }
        })
        columns.add(TableColumn<ResourceAttribute, String>("value").apply {
            cellValueFactory = Callback<ResourceAttributeCell, ObservableValue<String>> {
                val attribute: ResourceAttribute = it.value
                ReadOnlyStringWrapper(attribute.value)
            }
            cellFactory = Callback { RmlTableViewCell(rmlTableView, editor) }
            onEditCommit = EventHandler { event ->
                val rmlTag: ResourceTag = currentRmlTag ?: return@EventHandler
                val newValue: String = event.newValue
                val oldValue: String = event.oldValue
                if (newValue != oldValue) {
                    val (key, _) = selectionService.selectedResourceAttribute ?: return@EventHandler
                    editor.updateEditorUi {
                        editor.updateModel(rmlTag, ResourceAttribute(key, newValue))
                    }
                }
            }
        })
        selectionModel.selectedItemProperty().onChange {
            if (selectionModel.selectedItem != null) {
                selectionService.selectedResourceAttribute = selectionModel.selectedItem
            }
        }
        selectionService.selections.onChange { updateRmlTag() }
        rmlTreeView.selectionModel.selectedItemProperty().onChange { updateRmlTag() }
    }

    fun updateRmlTag() = runOnJfxThread {
        items.clear()
        val rmlTag = selectionService.selectedRmlTag
        if (rmlTag != null) {
            val selectedIndex = if (currentRmlTag === rmlTag) selectionModel.selectedIndex else 0
            currentRmlTag = rmlTag
            keysResult.clear()
            keysResult.addAll(editor.findAttributeKeys(rmlTag))
            for (feature in editor.features) {
                feature.filterAttributeKeys(rmlTag, keysResult)
            }
            for (key in keysResult) {
                items.add(ResourceAttribute(key, rmlTag.attributes[key] ?: ""))
            }
            selectionModel.select(selectedIndex)
        }
        requestResize()
    }

}
