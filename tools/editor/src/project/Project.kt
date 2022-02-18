package featurea.studio.project

import featurea.config.Config
import featurea.content.ResourceTag
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FSMenuBar
import featurea.desktop.jfx.confirmDialog
import featurea.desktop.runOnJfxThread
import featurea.desktop.watchOnJfxThread
import featurea.jvm.normalizedPath
import featurea.rml.RmlResource
import featurea.rml.deepCopy
import featurea.rml.findPropertyByIdPathOrNull
import featurea.rml.readRmlResource
import featurea.rml.reader.RmlContent
import featurea.rml.writer.RmlSerializer
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.ModuleBlock
import featurea.runtime.import
import featurea.studio.ProjectMenuBarProxy
import featurea.studio.editor.Editor
import featurea.studio.editor.components.EditorTab
import featurea.studio.home.StudioPanel
import featurea.studio.project.components.ProjectTabPanel
import featurea.utils.Property
import featurea.utils.watchOnApplicationThread
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.stage.Stage
import java.io.File

class Project(override val module: Module) : Component {

    val mainStage: Stage = import(MainStageProxy)
    val menuBar: FSMenuBar = import(ProjectMenuBarProxy)
    val rmlContent: RmlContent = import()
    val studioPanel = import<StudioPanel>()
    val tabPanel: ProjectTabPanel by lazy { import() } // quickfix todo decouple project from tabPanel

    lateinit var delegate: ProjectDelegate
    private val editorModuleMap = mutableMapOf<Tab, Module>()
    val editors: List<Editor> get() = editorModuleMap.values.map { it.importComponent<Editor>() }
    val isDirty: Boolean get() = editors.find { it.tab.isDirty } != null
    val rmlFileProperty = Property<File>()
    var rmlFile: File by rmlFileProperty
    val rmlResourceProperty = Property<RmlResource>()
    var rmlResource: RmlResource by rmlResourceProperty
    private val uiConfig = Config("ui")

    init {
        mainStage.onCloseRequest = EventHandler {
            if (isDirty) {
                confirmDialog("Save", "Save ${mainStage.title}?") {
                    for (editor in editors) {
                        save(editor.tab.rmlResource.rmlTag)
                    }
                }
            }
            ProjectContainerCounter--
            if (ProjectContainerCounter == 0) {
                studioPanel.launch()
            }
        }
    }

    init {
        rmlFileProperty.watchOnJfxThread {
            mainStage.title = rmlFile.normalizedPath
        }
        rmlFileProperty.watchOnApplicationThread {
            rmlResource = rmlContent.readRmlResource(rmlFile.normalizedPath)
        }
    }

    fun findSelectedEditorOrNull(): Editor? {
        val tab = tabPanel.selectionModel.selectedItem ?: return null
        val editorModule = editorModuleMap[tab] ?: return null
        val editor: Editor = editorModule.importComponent()
        return editor
    }

    fun addEditorModuleTab(editorModule: Module, tab: Tab) {
        editorModuleMap[tab] = editorModule
        tab.onCloseRequest = EventHandler { event ->
            closeDocument(tab)
        }
        editorModule.importComponent<EditorTab>().apply {
            this.tab = tab
        }
        runOnJfxThread {
            tabPanel.tabs.add(tab)
            tabPanel.selectionModel.select(tab)
        }
    }

    fun closeDocument(editorTab: Tab) {
        val editorModule = editorModuleMap.remove(editorTab) ?: return
        val editor: Editor = editorModule.importComponent()
        if (editor.tab.isDirty) {
            confirmDialog("Save", "Save ${editor.tab.id}?") {
                editor.tab.save()
            }
        }
        editorModule.destroy()

        tabPanel.selectionModel.clearSelection()
        tabPanel.tabs.remove(editorTab)
    }

    fun removeDocument(name: String) =
        confirmDialog(uiConfig["deleteDocumentTitle"], "${uiConfig["deleteDocumentTitle"]} $name?") {
            // 1. close tab
            val tab = findTabByName(name)
            if (tab != null) closeDocument(tab)

            // 2. remove documentRmlTag from projectRmlTag
            val projectRmlTag = rmlResource.rmlTag
            val documentRmlTag = projectRmlTag.properties.remove(name)
            projectRmlTag.children.remove(documentRmlTag)

            // 3. save
            save(documentRmlTag)
            rmlResourceProperty.notifyWatchers() // todo avoid explicit watcher notifications
        }

    fun importRmlTag(rmlTag: ResourceTag) {
        val name: String = rmlTag.idOrNull ?: error("rmlTag: $rmlTag")
        val projectRmlTag: ResourceTag = rmlResource.rmlTag
        projectRmlTag.properties[name] = rmlTag
        projectRmlTag.children.add(rmlTag)
        save(rmlTag)
    }

    fun openDocument(documentId: String, complete: ModuleBlock = {}) {
        val existingTab = tabPanel.tabs.find { it.text == documentId } // quickfix todo improve
        if (existingTab != null) {
            tabPanel.selectionModel.select(existingTab)
        } else {
            delegate.openDocument(documentId, complete)
        }
    }

    fun save(documentTag: ResourceTag?) {
        val projectRmlTag = rmlResource.rmlTag
        if (documentTag != null) {
            val documentTagCopy = documentTag.deepCopy(parent = projectRmlTag)
            val id = documentTagCopy.idOrNull
            if (id != null) {
                val currentRmlTag = projectRmlTag.findPropertyByIdPathOrNull(id)
                if (currentRmlTag != null) {
                    val index = projectRmlTag.children.indexOf(currentRmlTag)
                    projectRmlTag.children.remove(currentRmlTag)
                    projectRmlTag.children.add(index, documentTagCopy)
                    projectRmlTag.properties[id] = documentTagCopy // https://stackoverflow.com/a/44960581/909169
                }
            }
        }
        RmlSerializer.serialize(projectRmlTag, rmlFile)
        rmlResourceProperty.notifyWatchers() // quickfix todo avoid explicit watcher notifications
    }

    fun findDocumentOrNull(id: String): ResourceTag? {
        return rmlResource.rmlTag.properties[id]
    }

    /*internals*/

    private fun findTabByName(documentId: String): Tab? = editorModuleMap.keys.find { it.text == documentId }

}
