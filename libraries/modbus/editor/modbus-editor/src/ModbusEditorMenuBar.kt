package featurea.modbus.editor

import featurea.desktop.MainStageProxy
import featurea.desktop.runOnJfxThread
import featurea.desktop.jfx.FSMenuBar
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane

class ModbusEditorMenuBar(override val module: Module) : Component, FSMenuBar() {

    private val rootPane: BorderPane = import(MainStageProxy).scene.root as BorderPane

    init {
        rootPane.top = this
    }

}

class OpenMenu(override val module: Module) : Component {
    private val modbusEditor: ModbusEditor = import()
    private val modbusEditorMenuBar: ModbusEditorMenuBar = import()

    override fun onCreateComponent() {
        runOnJfxThread {
            modbusEditorMenuBar.findMenuItem("File", "Open Config...").apply {
                onAction = EventHandler { modbusEditor.openModbusConfig() }
                accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }
}

class CreateMenu(override val module: Module) : Component {
    private val modbusEditor: ModbusEditor = import()
    private val modbusEditorMenuBar: ModbusEditorMenuBar = import()

    override fun onCreateComponent() {
        runOnJfxThread {
            modbusEditorMenuBar.findMenuItem("File", "New Config...").apply {
                onAction = EventHandler { modbusEditor.createModbusConfig() }
                accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }
}

class CloseMenu(override val module: Module) : Component {
    private val modbusEditor: ModbusEditor = import()
    private val modbusEditorMenuBar: ModbusEditorMenuBar = import()

    override fun onCreateComponent() {
        runOnJfxThread {
            modbusEditorMenuBar.findMenuItem("File", "Close Config").apply {
                onAction = EventHandler { modbusEditor.close() }
                accelerator = KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }
}

class SaveMenu(override val module: Module) : Component {
    private val modbusEditor: ModbusEditor = import()
    private val modbusEditorMenuBar: ModbusEditorMenuBar = import()

    override fun onCreateComponent() {
        runOnJfxThread {
            modbusEditorMenuBar.findMenuItem("File", "Save").apply {
                onAction = EventHandler { modbusEditor.selectedDocument?.save() }
                accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }
}

class DeleteMenu(override val module: Module) : Component {
    private val modbusEditor: ModbusEditor = import()
    private val modbusEditorMenuBar: ModbusEditorMenuBar = import()

    override fun onCreateComponent() {
        runOnJfxThread {
            modbusEditorMenuBar.findMenuItem("File", "Delete").apply {
                onAction = EventHandler { modbusEditor.selectedDocument?.removeSelected() }
                accelerator = KeyCodeCombination(KeyCode.DELETE)
            }
        }
    }
}

