package featurea.studio.editor.components

import featurea.content.ResourceAttribute
import featurea.content.ResourceSchema
import featurea.content.ResourceTag
import featurea.desktop.jfx.CellUtils
import featurea.desktop.jfx.CellUtils.defaultStringConverter
import featurea.desktop.jfx.fireEditEvent
import featurea.desktop.jfx.onDoubleClick
import featurea.desktop.jfx.rowItem
import featurea.utils.splitAndTrim
import featurea.studio.editor.Editor
import featurea.studio.editor.components.CellType.*
import featurea.studio.project.components.openExternalEditor
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.event.EventHandler
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TableCell
import javafx.scene.control.TextField

class RmlTableViewCell(val rmlTableView: RmlTableView, val editor: Editor) : TableCell<ResourceAttribute, String>() {

    private val externalEditors = rmlTableView.externalEditors
    private val enums = rmlTableView.enums
    private val rmlSchema: ResourceSchema get() = editor.tab.rmlResource.rmlFile.rmlSchema

    private var type: CellType? = null
    private var textField: TextField? = null
    private var checkBox: CheckBox? = null
    private var choiceBox: ChoiceBox<String>? = null

    private val currentRmlTag: ResourceTag get() = rmlTableView.currentRmlTag ?: error("currentRmlTag not defined")

    override fun cancelEdit() {
        super.cancelEdit()
        when (type) {
            TEXT -> {
                CellUtils.cancelEdit(this, defaultStringConverter(), null)
            }
            FLAG -> {
                // no op
            }
            ENUM -> {
                text = defaultStringConverter<String>().toString(item)
                graphic = null
            }
        }
    }

    override fun startEdit() {
        when (type) {
            TEXT -> {
                if (!isEditable || !tableView.isEditable || !tableColumn.isEditable) return
                super.startEdit()
                if (isEditing) CellUtils.startEdit(this, defaultStringConverter(), null, null, textField)
            }
            FLAG -> {
                super.startEdit()
            }
            ENUM -> {
                if (!isEditable || !tableView.isEditable || !tableColumn.isEditable) return
                super.startEdit()
                val choiceBox: ChoiceBox<String> = choiceBox ?: error("choiceBox: null")
                text = null
                graphic = choiceBox
                choiceBox.show()
            }
        }
    }

    override fun updateItem(item: String?, isEmpty: Boolean) {
        super.updateItem(item, isEmpty)

        fun resetType() {
            type = null
            textField = null
            checkBox = null
            choiceBox = null
            text = null
            graphic = null
        }
        resetType()

        if (item != null) {
            val attribute: ResourceAttribute = rowItem

            type = chooseCellType()
            when (type) {
                TEXT -> {
                    CellUtils.updateItem(this, defaultStringConverter(), null, null, textField)
                }
                FLAG -> {
                    val checkBox: CheckBox = checkBox ?: error("checkBox: null")
                    graphic = checkBox
                    checkBox.isSelected = attribute.value == "true"
                }
                ENUM -> {
                    CellUtils.updateItem(this, defaultStringConverter(), null, null, choiceBox)
                }
            }

            // 2. action
            val scriptId: String = "${currentRmlTag.name}.${attribute.key}"
            val script: String? = externalEditors[scriptId]
            if (script == null) {
                isEditable = true
                onMouseClickedProperty().set(null)
            } else {
                isEditable = false
                onDoubleClick {
                    rmlTableView.openExternalEditor<String>(script, attribute.value, editor) { result ->
                        val resultFiltered: String = editor.filterAttribute(currentRmlTag, attribute.key, result)
                        fireEditEvent(resultFiltered)
                    }
                }
            }
        }
    }

    /*internals*/

    private fun chooseCellType(): CellType {
        val attribute: ResourceAttribute = rowItem
        val canonicalName = rmlSchema.canonicalClassNameByKey["${currentRmlTag.name}.${attribute.key}"]
            ?: error("canonicalName not found")

        // 1. FLAG
        if (canonicalName == "Boolean") {
            checkBox = CheckBox().apply {
                onAction = EventHandler {
                    editor.updateEditorUi {
                        editor.updateModel(currentRmlTag, ResourceAttribute(attribute.key, isSelected.toString()))
                    }
                }
            }
            return FLAG
        }

        // 2. ENUM
        val enumValues: String? = enums[canonicalName]
        if (enumValues != null) {
            val items = observableArrayList(enumValues.splitAndTrim(","))
            choiceBox = CellUtils.createChoiceBox(this, items, SimpleObjectProperty()).apply {
                // >> IMPORTANT order has meaning
                // 1.
                val item = attribute.value
                if (item.isNotBlank()) selectionModel.select(item)

                // 2.
                onAction = EventHandler {
                    editor.updateEditorUi {
                        editor.updateModel(currentRmlTag, ResourceAttribute(attribute.key, selectionModel.selectedItem))
                    }
                }
                // <<
            }
            return ENUM
        }

        // 3. TEXT
        textField = CellUtils.createTextField(this, defaultStringConverter())
        return TEXT
    }

}
