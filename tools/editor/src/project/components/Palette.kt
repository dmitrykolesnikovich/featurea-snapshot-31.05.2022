package featurea.studio.project.components

import featurea.Application
import featurea.config.Config
import featurea.content.ResourceTag
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.*
import featurea.desktop.runOnJfxThread
import featurea.Scope
import featurea.rml.deepCopy
import featurea.runOnUpdateOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.home.components.DefaultsService
import featurea.studio.project.Project
import featurea.toSimpleName
import featurea.utils.Property
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode.ESCAPE
import javafx.stage.Stage
import javafx.stage.StageStyle.UTILITY
import javafx.util.Callback

class Palette(override val module: Module) : Component {

    private val defaultsService: DefaultsService = import()
    private val mainStage: Stage = import(MainStageProxy)
    private val project: Project = import()

    private val uiConfig = Config("ui")
    private val paletteConfig = Config("palette")
    private val iconsConfig = Config("icons")

    val paletteTagProperty = Property<String?>()
    var paletteTag: String? by paletteTagProperty
        private set
    val documentTagProperty = Property<String?>()
    var documentTag: String? by documentTagProperty
    private val stage = Stage().registerGlobalKeyEvents(mainStage)
    private val tableView: PaletteTableView = PaletteTableView()
    var hasAppend: Boolean = false
    val isActive: Boolean get() = paletteTag != null || documentTag != null
    var instance: Any? = null
        private set

    init {
        tableView.onSelectionChange { objectIcon ->
            // init
            deactivate()

            // filter
            @Suppress("NAME_SHADOWING")
            val objectIcon: ObjectIcon = objectIcon ?: return@onSelectionChange

            // action
            if (objectIcon.isComplex) {
                val selectedEditor = project.findSelectedEditorOrNull()
                documentTag = when {
                    selectedEditor == null -> null
                    selectedEditor.tab.id == objectIcon.title -> {
                        clearSelection()
                        null
                    }
                    else -> objectIcon.title
                }
            } else {
                paletteTag = objectIcon.tag
            }
        }
        stage.apply {
            title = uiConfig["paletteStageTitle"]
            isResizable = true
            scene = Scene(tableView, 240.0, 600.0)
            initStyle(UTILITY)
            sizeToScene()
        }

        onKeyEvent { key -> if (key.code == ESCAPE) clearSelection() }
    }

    init {
        project.tabPanel.selectionModel.selectedItemProperty().onChange { selectedItem ->
            if (selectedItem != null) {
                updateInstance()
            } else {
                deactivate()
            }
        }
        paletteTagProperty.watch {
            if (paletteTag != null) {
                updateInstance()
            } else {
                deactivate()
            }
        }
        documentTagProperty.watch {
            if (documentTag != null) {
                updateInstance()
            } else {
                deactivate()
            }
        }
        project.rmlResourceProperty.watch { updateTableView() }
    }

    fun show() {
        stage.show()
    }

    fun createRmlTag(): ResourceTag? = createPaletteRmlTag() ?: createDocumentRmlTag()

    /*internals*/

    private fun createPaletteRmlTag(): ResourceTag? {
        val paletteTag: String = paletteTag ?: return null
        return defaultsService.createDefaultRmlTag(paletteTag)
    }

    private fun createDocumentRmlTag(): ResourceTag? {
        val documentId = documentTag ?: return null
        val documentRmlTag = project.rmlResource.rmlTag.properties[documentId] ?: return null
        return documentRmlTag.deepCopy()
    }

    private fun clearSelection() = runOnJfxThread {
        tableView.selectionModel.clearSelection()
    }

    private fun updateTableView() {
        if (!paletteConfig.exists()) return

        tableView.update(mutableListOf<ObjectIcon>().apply {
            addAll(paletteConfig.properties.map { ObjectIcon(it.key, it.key.toSimpleName(), it.value, false) })
            addAll(project.rmlResource.rmlTag.properties.map {
                val name: String = it.value.name
                val title: String = it.value.idOrNull ?: error("value: ${it.value}")
                val icon: String = iconsConfig[name] ?: error("iconsConfig: $iconsConfig")
                ObjectIcon(name, title, icon, true)
            })
        })
    }

    private fun updateInstance() {
        val selectedEditor: Editor? = project.findSelectedEditorOrNull()
        if (selectedEditor == null) {
            deactivate()
            return
        }
        val app: Application = selectedEditor.import()
        app.runOnUpdateOnJfxThread {
            with(selectedEditor.tab.rmlResource) {
                instance = createRmlTag()?.createRmlTagEndObject(Scope.OUTER)
            }
        }
    }

    private fun deactivate() {
        documentTag = null
        paletteTag = null
        instance = null
    }

}

/*internals*/

private fun String.replaceCounter(counter: Int): String = replace("\\$\\{counter\\}".toRegex(), "$counter")

private data class ObjectIcon(val tag: String, val title: String, val icon: String, val isComplex: Boolean)

private typealias ObjectIconCell = TableColumn.CellDataFeatures<ObjectIcon, String>

private typealias PaletteCell = TableColumn.CellDataFeatures<ObjectIcon, ImageView>

private class PaletteTableView : FSTableView<ObjectIcon>() {

    init {
        gridLinesVisible = false
        columns.addAll(
            TableColumn<ObjectIcon, ImageView>("icon").apply {
                minWidth = 32.0
                editableProperty().set(false)
                cellValueFactory = Callback<PaletteCell, ObservableValue<ImageView>> {
                    SimpleObjectProperty(it.value.icon.toImageView().apply { fitWidth = 32.0; isPreserveRatio = true })
                }
            },
            TableColumn<ObjectIcon, String>("title").apply {
                editableProperty().set(false)
                cellValueFactory = Callback<ObjectIconCell, ObservableValue<String>> {
                    ReadOnlyStringWrapper(it.value.title)
                }
            })
    }

    fun update(objectIcons: List<ObjectIcon>) {
        items.clear()
        items.addAll(objectIcons)
    }

}
