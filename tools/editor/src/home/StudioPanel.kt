package featurea.studio.home

import featurea.icons.Resources
import featurea.config.Config
import featurea.desktop.MainStageProxy
import featurea.desktop.Preferences
import featurea.desktop.jfx.fillWidth
import featurea.desktop.jfx.toImageOrNull
import featurea.desktop.jfx.toImageView
import featurea.desktop.jfx.withAlignment
import featurea.jvm.normalizedPath
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.splitAndTrim
import featurea.studio.project.components.openExternalEditor
import featurea.truncatePath
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos.*
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.layout.GridPane.setMargin
import javafx.scene.layout.GridPane.setVgrow
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.Priority.NEVER
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Callback
import java.io.File

class StudioOptions(val packageId: String, val delegate: StudioDelegate)

class StudioPanel(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)

    private val buildConfig: Config = Config("build")
    private val recentProjectsPreferences: Preferences = Preferences("recentProjects")
    private val uiConfig: Config = Config("ui")

    private val contentPanel: GridPane = GridPane()
    private val labelBox: VBox = VBox()
    private val projectListView: ListView<String> = ListView<String>()
    lateinit var options: StudioOptions
        private set
    private val logoImageView: ImageView = ImageView(uiConfig["studio.icon"]?.toImageOrNull()).apply {
        fitWidth = 64.0
        fitHeight = 64.0
    }
    private val openProjectLabel: Label = Label("Open Project")
    private val createNewProjectLabel: Label = Label("Create New Project")
    private val aboutLabel: Label = Label("Version - Build")
    // private val copyExistingProjectLabel = Label("Copy Existing Project")
    // private val createNewBundleLabel = Label("Create New Bundle")
    private val uploadBundleLabel = Label("Upload Bundle")

    init {
        contentPanel.apply {
            padding = Insets(4.0)
            hgap = 4.0
            vgap = 4.0

            rowConstraints.add(RowConstraints().apply { vgrow = ALWAYS })
            rowConstraints.add(RowConstraints().apply { vgrow = NEVER })
            rowConstraints.add(RowConstraints().apply { vgrow = NEVER })

            columnConstraints.add(ColumnConstraints().apply {
                minWidth = 320.0
                hgrow = Priority.SOMETIMES
            })

            columnConstraints.add(ColumnConstraints().apply {
                minWidth = 280.0
                hgrow = Priority.ALWAYS
            })

            add(projectListView, 0, 0, 1, 2)

            add(logoImageView.withAlignment(CENTER) { maxHeight = 140.0 }, 1, 0)

            add(VBox().apply {
                alignment = CENTER
                setVgrow(this, NEVER)
                children.add(labelBox.apply {
                    spacing = 10.0
                    isFillWidth = false
                    maxWidth = Double.NEGATIVE_INFINITY
                    setVgrow(this, NEVER)
                })
                setMargin(this, Insets(0.0, 0.0, 20.0, 0.0))
            }, 1, 1)

            add(aboutLabel.fillWidth(halignment = HPos.CENTER), 0, 2, 2, 1)
        }


        appendLabel(openProjectLabel)
        appendLabel(createNewProjectLabel)
        /*
        appendLabel(copyExistingProjectLabel)
        appendLabel(createNewBundleLabel)
        */
        appendLabel(uploadBundleLabel)
        setupAboutLabel()

        openProjectLabel.onMouseClicked = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Project files (*.project)", "*.project"))
            val file = fileChooser.showOpenDialog(mainStage)
            if (file != null && file.exists()) {
                openProject(file)
            }
        }

        createNewProjectLabel.onMouseClicked = EventHandler {
            val file = FileChooser().apply {
                extensionFilters.add(FileChooser.ExtensionFilter("Project files (*.project)", "*.project"))
            }.showSaveDialog(mainStage)
            if (file != null) {
                options.delegate.newProject(file)
                openProject(file)
            }
        }

        uploadBundleLabel.onMouseClicked = EventHandler {
            openExternalEditor<Any>("featurea.deviceChooser.studio.DevicesChooserDialog.show()")
        }

        projectListView.cellFactory = Callback<ListView<String>, ListCell<String>> {
            object : ListCell<String>() {
                override fun updateItem(item: String?, isEmpty: Boolean) {
                    super.updateItem(item, isEmpty)
                    if (isEmpty) {
                        graphic = null
                    } else {
                        @Suppress("NAME_SHADOWING")
                        val item: String = item ?: error("item: null")
                        graphic = HBox().apply {
                            children.add(StackPane().apply {
                                HBox.setHgrow(this, ALWAYS)
                                children.add(Label(item.truncatePath(40)).apply {
                                    maxWidth = 300.0 - 24.0
                                    StackPane.setAlignment(this, CENTER_LEFT)
                                })
                            })
                            children.add(StackPane().apply {
                                HBox.setHgrow(this, NEVER)
                                children.add(Label().apply {
                                    StackPane.setAlignment(this, CENTER_RIGHT)
                                    alignment = CENTER
                                    graphic = Resources.closePng.toImageView()
                                    setPrefSize(24.0, 24.0)
                                    onMouseClicked = EventHandler { event ->
                                        val selectedIndex: Int = projectListView.selectionModel.selectedIndex
                                        val filePath: String? = projectListView.items.removeAt(selectedIndex)
                                        recentProjectsPreferences.edit {
                                            properties["list"] = properties.getOrDefault("list", "").splitAndTrim(",")
                                                .toMutableSet().apply { remove(filePath) }.joinToString()
                                        }
                                        projectListView.selectionModel.clearSelection()
                                        event.consume()
                                    }
                                })
                            })
                        }
                    }
                }
            }
        }

        projectListView.onMouseClicked = EventHandler { event ->
            if (event.clickCount == 2) {
                val selectedIndex = projectListView.selectionModel.selectedIndex
                val filePath = projectListView.items[selectedIndex]
                val file = File(filePath)
                openProject(file)
                event.consume()
            }
        }
    }

    fun appendLabel(label: Label) {
        labelBox.children.add(label)
    }

    private fun setupAboutLabel() {
        val version: String? = buildConfig["lcontrol.scada.editor.version"]
        val date: String? = buildConfig["lcontrol.scada.editor.date"]
        aboutLabel.text = "Build $version / $date"
    }

    private fun openProject(file: File) {
        mainStage.hide()
        recentProjectsPreferences.edit {
            val tokens: List<String> = this["list", ""].splitAndTrim(",")
            this["list"] = tokens.toMutableSet().apply { add(file.normalizedPath) }.joinToString()
        }
        options.delegate.openProject(file)
    }

    fun launch(options: StudioOptions = this.options) {
        this.options = options
        if (mainStage.scene == null || mainStage.scene.root != contentPanel) {
            mainStage.scene = Scene(contentPanel, 600.0, 300.0)
        }
        mainStage.sizeToScene()
        projectListView.items.clear()
        projectListView.items.addAll(recentProjectsPreferences["list", ""].splitAndTrim(",").filter {
            val file = File(it)
            file.exists() && file.readLines()[0].startsWith("<rml package=\"${options.packageId}\"")
        })
        projectListView.refresh()
        mainStage.show()
    }

}

fun Component.installStudioPanelAction(title: String, action: () -> Unit) {
    val studioPanel: StudioPanel = import()
    studioPanel.appendLabel(Label(title).apply {
        onMouseClicked = EventHandler {
            action()
        }
    })
}
