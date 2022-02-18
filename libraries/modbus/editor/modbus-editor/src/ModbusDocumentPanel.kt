package featurea.modbus.editor

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.RmlTableView
import featurea.studio.editor.components.RmlTreeView
import javafx.scene.control.SplitPane
import featurea.runtime.Provide
import featurea.runtime.provide
import featurea.desktop.MainNodeProxy

@Provide(MainNodeProxy::class)
class ModbusDocumentPanel(override val module: Module) : Component, SplitPane() {

    private val rmlTreeView: RmlTreeView = import()
    private val rmlTableView: RmlTableView = import()

    init {
        items.add(rmlTreeView)
        items.add(rmlTableView)
        provide(MainNodeProxy(this))
    }

}
