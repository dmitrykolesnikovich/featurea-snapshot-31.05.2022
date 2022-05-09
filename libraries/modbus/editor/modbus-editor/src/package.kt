package featurea.modbus.editor

import featurea.runtime.*
import featurea.script.Script
import featurea.utils.*
import java.io.File

/*content*/

object Resources {
    val ChannelLocalPng = "featurea/modbus/editor/ChannelLocal.png"
    val ChannelPhysicalPng = "featurea/modbus/editor/ChannelPhysical.png"
    val ChannelVirtualPng = "featurea/modbus/editor/ChannelVirtual.png"
    val clockPng = "featurea/modbus/editor/clock@12.png"
    val scriptPng = "featurea/modbus/editor/script@12.png"
    val localPng = "featurea/modbus/editor/local.png"
    val localTimeoutPng = "featurea/modbus/editor/localTimeout.png"
    val localWriteEventPng = "featurea/modbus/editor/localWriteEvent.png"
    val modbusPng = "featurea/modbus/editor/modbus.png"
    val virtualPng = "featurea/modbus/editor/virtual.png"
    val virtualTimeoutPng = "featurea/modbus/editor/virtualTimeout.png"
    val virtualWriteEventPng = "featurea/modbus/editor/virtualWriteEvent.png"
}

/*dependencies*/

val artifact = Artifact("featurea.modbus.editor") {
    includeContentRootWithConfig { "$featureaDir/libraries/modbus/editor/modbus-editor/res" }
    include(featurea.modbus.artifact)
    include(featurea.studio.artifact)
    "Docket" to ::Docket

    "ModbusEditor" to ::ModbusEditor
    "ModbusEditorContainer" to ::ModbusEditorContainer
    "ModbusEditorModule" to ::ModbusEditorModule
    "document.ModbusDocument" to ::ModbusDocument
    "document.ModbusDocumentStage" to ::ModbusDocumentStage
    "document.ModbusDocumentToolbar" to ::ModbusDocumentToolbar
    "document.ModbusDocumentPanel" to ::ModbusDocumentPanel

    ModbusDocumentPlugin {
        "ModbusDocumentFeature" to ::ModbusDocumentFeature
    }

    ModbusEditorPlugin {
        "CreateMenu" to ::CreateMenu
        "OpenMenu" to ::OpenMenu
        "CloseMenu" to ::CloseMenu
        "SaveMenu" to ::SaveMenu
        "DeleteMenu" to ::DeleteMenu
        "SaveStationMenu" to ::SaveStationMenu
        "ModbusEditorMenuBar" to ::ModbusEditorMenuBar
    }
}

fun DependencyBuilder.ModbusDocumentPlugin(plugin: Plugin<ModbusDocument>) = install(plugin)

/*dockets*/

class Docket(override val module: Module) : Component, Script {

    private val stage: ModbusDocumentStage = import()

    override suspend fun execute(action: String, args: List<Any?>, scope: Scope): Unit {
        when (action) {
            "ModbusDocumentStage.show" -> stage.show(args[0] as File)
        }
        return Unit
    }

}

/*instrumentation*/

fun configureInstrumentation() {
    enableInstrumentation()
    val lcontrolDir: String = systemProperty("lcontrolDir") ?: "" // quickfix todo improve
    Tools["scada"] = "$lcontrolDir/modules/scada/scada-cli/build/install/scada-cli-shadow/bin/scada"
}
