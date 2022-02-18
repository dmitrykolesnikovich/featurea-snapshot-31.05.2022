package featurea.modbus.editor

import featurea.desktop.MainStageProxy
import featurea.jvm.createNewFileAndDirs
import featurea.runtime.*
import featurea.studio.editor.components.EditorTab
import featurea.studio.project.Project
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import java.io.File

private val fileChooser = FileChooser().apply {
    extensionFilters.add(FileChooser.ExtensionFilter("Modbus Config Files", "*.mdb"))
}

class ModbusEditor(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val project: Project = import()

    private val documents = mutableMapOf<Tab, ModbusDocument>()
    val selectedDocument: ModbusDocument? get() = documents[project.tabPanel.selectionModel.selectedItem]

    fun openModbusConfig() {
        val file = fileChooser.showOpenDialog(mainStage)
        if (file != null) {
            open(file)
        }
    }

    fun createModbusConfig() {
        val file = fileChooser.showSaveDialog(mainStage)
        if (file != null) {
            file.createNewFileAndDirs()
            file.writeText(
                """
                <rml package="featurea.modbus">
                    <Directory id="config"/>
                </rml>
                """.trimIndent()
            )
            open(file)
        }
    }

    fun open(file: File) {
        defaultProxyScope {
            exportComponents(featurea.modbus.editor.artifact)
            appendDefaultContainer {
                include(module.container)
            }
            injectDefaultModule {
                include(module)
            }
            complete { module ->
                runBlocking {
                    val modbusDocument: ModbusDocument = module.importComponent()
                    val modbusDocumentToolbar: ModbusDocumentToolbar = module.importComponent()

                    val tab: Tab = Tab(file.name)
                    tab.content = BorderPane().apply {
                        top = modbusDocumentToolbar
                        center = modbusDocument.panel
                    }
                    project.tabPanel.tabs.add(tab)
                    project.tabPanel.selectionModel.select(tab)
                    module.importComponent<EditorTab>().apply { this.tab = tab }
                    documents[tab] = modbusDocument
                    modbusDocument.initFile(file)
                }
            }
        }
    }

    fun close() {
        project.tabPanel.tabs.remove(project.tabPanel.selectionModel.selectedItem)
    }

}

fun DependencyBuilder.ModbusEditorPlugin(plugin: Plugin<ModbusEditor>) = install(plugin)
