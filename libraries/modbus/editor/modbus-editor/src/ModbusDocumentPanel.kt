package featurea.modbus.editor

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.RmlTableView
import featurea.studio.editor.components.RmlTreeView
import featurea.runtime.Provide
import featurea.runtime.provide
import featurea.desktop.MainNodeProxy
import featurea.desktop.jfx.FSSplitPane

@Provide(MainNodeProxy::class)
class ModbusDocumentPanel(override val module: Module) : Component, FSSplitPane() {

    private val rmlTreeView: RmlTreeView = import()
    private val rmlTableView: RmlTableView = import()

    init {
        items.add(rmlTreeView)
        items.add(rmlTableView)
        provide(MainNodeProxy(this))
    }

}
