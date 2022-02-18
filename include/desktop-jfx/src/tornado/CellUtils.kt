package featurea.desktop.jfx

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.util.StringConverter

object CellUtils {

    private val defaultStringConverter = object : StringConverter<Any>() {

        override fun toString(value: Any?): String? {
            return value?.toString()
        }

        override fun fromString(string: String): Any {
            return string
        }

    }

    private val defaultTreeItemStringConverter = object : StringConverter<TreeItem<*>>() {

        override fun toString(treeItem: TreeItem<*>?): String {
            return if (treeItem == null || treeItem.value == null) "" else treeItem.value.toString()
        }

        override fun fromString(string: String): TreeItem<*> {
            return TreeItem(string)
        }

    }

    fun <T> defaultStringConverter(): StringConverter<T> {
        return defaultStringConverter as StringConverter<T>
    }

    fun <T> defaultTreeItemStringConverter(): StringConverter<TreeItem<T>> {
        return defaultTreeItemStringConverter as StringConverter<TreeItem<T>>
    }

    private fun <T> getItemText(cell: Cell<T>, converter: StringConverter<T>?): String {
        return if (converter == null) {
            if (cell.item == null) "" else cell.item.toString()
        } else {
            converter.toString(cell.item)
        }
    }

    fun <T> updateItem(cell: Cell<T>, converter: StringConverter<T>, choiceBox: ChoiceBox<T>) {
        updateItem(cell, converter, null, null, choiceBox)
    }

    fun <T> updateItem(cell: Cell<T>, converter: StringConverter<T>, hbox: HBox?, graphic: Node?, choiceBox: ChoiceBox<T>?) {
        if (cell.isEmpty) {
            cell.text = null
            cell.setGraphic(null)
        } else {
            if (cell.isEditing) {
                choiceBox?.selectionModel?.select(cell.item)
                cell.text = null

                if (graphic != null && hbox != null) {
                    hbox.children.setAll(graphic, choiceBox)
                    cell.setGraphic(hbox)
                } else {
                    cell.setGraphic(choiceBox)
                }
            } else {
                cell.text = getItemText(cell, converter)
                cell.setGraphic(graphic)
            }
        }
    }

    fun <T> createChoiceBox(cell: Cell<T>, items: ObservableList<T>, converter: ObjectProperty<StringConverter<T>>): ChoiceBox<T> {
        val choiceBox = ChoiceBox(items)
        choiceBox.maxWidth = java.lang.Double.MAX_VALUE
        choiceBox.converterProperty().bind(converter)
        choiceBox.selectionModel.selectedItemProperty().addListener { ov, oldValue, newValue ->
            if (cell.isEditing) {
                cell.commitEdit(newValue)
            }
        }
        return choiceBox
    }


    fun <T> updateItem(cell: Cell<T>, converter: StringConverter<T>, textField: TextField) {
        updateItem(cell, converter, null, null, textField)
    }

    fun <T> updateItem(cell: Cell<T>, converter: StringConverter<T>, hbox: HBox?, graphic: Node?, textField: TextField?) {
        if (cell.isEmpty) {
            cell.text = null
            cell.setGraphic(null)
        } else {
            if (cell.isEditing) {
                if (textField != null) {
                    textField.text = getItemText(cell, converter)
                }
                cell.text = null

                if (graphic != null && hbox != null) {
                    hbox.children.setAll(graphic, textField)
                    cell.setGraphic(hbox)
                } else {
                    cell.setGraphic(textField)
                }
            } else {
                cell.text = getItemText(cell, converter)
                cell.setGraphic(graphic)
            }
        }
    }

    fun <T> startEdit(cell: Cell<T>, converter: StringConverter<T>, hbox: HBox?, graphic: Node?, textField: TextField?) {
        if (textField != null) {
            textField.text = getItemText(cell, converter)
        }
        cell.text = null

        if (graphic != null && hbox != null) {
            hbox.children.setAll(graphic, textField)
            cell.setGraphic(hbox)
        } else {
            cell.setGraphic(textField)
        }
        textField?.selectAll()
        textField?.requestFocus()
    }

    fun <T> cancelEdit(cell: Cell<T>, converter: StringConverter<T>, graphic: Node?) {
        cell.text = getItemText(cell, converter)
        cell.graphic = graphic
    }

    fun <T> createTextField(cell: Cell<T>, converter: StringConverter<T>?): TextField {
        val textField = TextField(getItemText(cell, converter))

        textField.setOnAction { event ->
            checkNotNull(converter) {
                ("Attempting to convert text input into Object, but provided "
                        + "StringConverter is null. Be sure to set a StringConverter "
                        + "in your cell factory.")
            }
            cell.commitEdit(converter.fromString(textField.text))
            event.consume()
        }
        textField.setOnKeyReleased { t ->
            if (t.code == KeyCode.ESCAPE) {
                cell.cancelEdit()
                t.consume()
            }
        }
        return textField
    }

}
