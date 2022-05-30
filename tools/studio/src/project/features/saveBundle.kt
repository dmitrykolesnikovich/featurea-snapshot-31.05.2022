package featurea.studio.project

import featurea.BundleOptions
import featurea.System
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.*
import featurea.desktop.runOnJfxThread
import featurea.jvm.normalizedPath
import featurea.runtime.Action
import featurea.runtime.import
import featurea.studio.home.StudioPanel
import featurea.studio.project.components.openExternalEditor
import featurea.withExtension
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File

val saveBundle: Action = {
    val mainStage: Stage = import(MainStageProxy)
    val project: Project = import()
    val studioPanel: StudioPanel = import()
    val system: System = import()

    val progressBarDialog: FSProgressBarDialog = FSProgressBarDialog("Exporting Bundle")
    val projectFileTextField: TextField = TextField()
    val stage: Stage = Stage(mainStage).registerGlobalKeyEvents()
    val bundleFileTextField: TextField = TextField()

    runOnJfxThread {
        stage.title = "Export Bundle"
        val contentPanel = GridPane().apply {
            padding = Insets(8.0)
            hgap = 8.0
            vgap = 8.0
        }
        contentPanel.add(projectFileTextField.withLabel("Project File"), 0, 0)
        contentPanel.add(Button("..."), 1, 0)
        contentPanel.add(bundleFileTextField.withLabel("Bundle File"), 0, 1)
        contentPanel.add(Button("..."), 1, 1)
        contentPanel.add(Button("Export").apply {
            isDefaultButton = true
            onAction = EventHandler {
                val projectPath: String = projectFileTextField.text ?: return@EventHandler
                val bundlePath: String = bundleFileTextField.text ?: return@EventHandler

                stage.close()
                progressBarDialog.showAndWait()
                val options = BundleOptions(File(projectPath), File(bundlePath), system.contentRoots).apply {
                    command = "bundle"
                }
                studioPanel.options.delegate.createBundle(
                    options,
                    { progress ->
                        progressBarDialog.updateProgress(progress)
                    },
                    { exitCode ->
                        runOnJfxThread {
                            progressBarDialog.close()
                            if (exitCode == 0) {
                                infoDialog("Export Bundle", "Completed successfully.") {
                                    openExternalEditor<Any>(
                                        "featurea.deviceChooser.studio.DevicesChooserDialog.show(bundleFile)",
                                        bundlePath
                                    )
                                }
                            } else {
                                warningDialog("Export Bundle", "Error")
                            }
                        }
                    })
            }
        }.fillWidth(HPos.RIGHT), 0, 2, 2, 1)

        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS; valignment = VPos.BOTTOM })

        contentPanel.minWidth = 600.0
        stage.scene = Scene(contentPanel)
        stage.closeOnEscape()
        stage.initStyle(StageStyle.UTILITY)
        stage.sizeToScene()

        project.menuBar.findMenuItem("File", "Export Bundle...").apply {
            onAction = EventHandler {
                if (project.editors.any { it.tab.isDirty }) {
                    confirmDialog("Export Bundle", "Save all documents before exporting new bundle?") {
                        for (editor in project.editors) {
                            editor.tab.save()
                        }
                    }
                }
                projectFileTextField.text = project.rmlFile.normalizedPath
                bundleFileTextField.text = project.rmlFile.normalizedPath.withExtension("bundle")
                stage.show()
            }
            accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
        }
    }
}
