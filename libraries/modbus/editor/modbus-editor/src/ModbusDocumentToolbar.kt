package featurea.modbus.editor

import featurea.desktop.jfx.onChange
import featurea.desktop.jfx.toImageView
import featurea.icons.Resources.appendPng
import featurea.icons.Resources.removePng
import featurea.icons.Resources.copyPng
import featurea.rml.anyParent
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.RmlTreeView
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ToolBar

class ModbusDocumentToolbar(override val module: Module) : Component, ToolBar() {

    private val modbusDocument: ModbusDocument = import()
    private val rmlTreeView: RmlTreeView = import()

    private val appendDirectoryButton = Button("New Directory").apply {
        graphic = appendPng.toImageView()
        onAction = EventHandler {
            modbusDocument.appendDirectory()
        }
    }
    private val appendConnectionButton = Button("New Connection").apply {
        graphic = appendPng.toImageView()
        onAction = EventHandler {
            modbusDocument.appendConnection()
        }
    }
    private val appendPhysicalChannelButton = Button("New Physical Channel").apply {
        graphic = appendPng.toImageView()
        onAction = EventHandler {
            modbusDocument.appendPhysicalChannel()
        }
    }
    private val appendVirtualChannelButton = Button("New Virtual Channel").apply {
        graphic = appendPng.toImageView()
        onAction = EventHandler {
            modbusDocument.appendVirtualChannel()
        }
    }
    private val appendLocalChannelButton = Button("New Local Channel").apply {
        graphic = appendPng.toImageView()
        onAction = EventHandler {
            modbusDocument.appendLocalChannel()
        }
    }
    private val copyChannelButton = Button("Copy Channel").apply {
        graphic = copyPng.toImageView(16)
        onAction = EventHandler {
            modbusDocument.copyChannel()
        }
    }
    private val removeSelectedButton = Button("Delete").apply {
        graphic = removePng.toImageView()
        onAction = EventHandler {
            modbusDocument.removeSelected()
        }
    }

    init {
        items.add(appendDirectoryButton)
        items.add(appendConnectionButton)
        items.add(appendPhysicalChannelButton)
        items.add(appendVirtualChannelButton)
        items.add(appendLocalChannelButton)
        items.add(copyChannelButton)
        items.add(removeSelectedButton)
        rmlTreeView.selectionModel.selectedItemProperty().onChange { treeItem ->
            if (treeItem == null) {
                appendDirectoryButton.isDisable = true
                appendConnectionButton.isDisable = true
                appendPhysicalChannelButton.isDisable = true
                copyChannelButton.isDisable = true
                removeSelectedButton.isDisable = true
            } else {
                val rmlTag = treeItem.value
                appendDirectoryButton.isDisable = false
                appendConnectionButton.isDisable =
                    rmlTag.name == "Connection" || rmlTag.anyParent { it.name == "Connection" }
                appendPhysicalChannelButton.isDisable = !appendConnectionButton.isDisable
                copyChannelButton.isDisable = rmlTag.name != "Channel"
                removeSelectedButton.isDisable = false
            }
        }
    }

}
