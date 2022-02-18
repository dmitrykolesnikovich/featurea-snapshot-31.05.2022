package featurea.deviceChooser.studio;

import featurea.launchCommand
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.*
import featurea.fileTransfer.FileTransfer
import featurea.fileTransfer.FileTransferEventListener
import featurea.fileTransfer.FtpServer
import featurea.jvm.normalizedPath
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.script.Script
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Callback
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.concurrent.thread
import featurea.log

private typealias DevicesChooserCell = TableColumn.CellDataFeatures<FtpServer, String>

class DevicesChooserDialog(override val module: Module) : Component, Stage() {

    private val mainStage: Stage = import(MainStageProxy)

    private val progressBarDialog = FSProgressBarDialog("Upload Bundle")
    private val ftpServersTableView = FSTableView<FtpServer>().apply {
        selectionModel.selectionMode = SelectionMode.SINGLE
        placeholder = Label("<none>")
        columns.add(TableColumn<FtpServer, String>("Model").apply {
            isEditable = true
            cellValueFactory = Callback<DevicesChooserCell, ObservableValue<String>> {
                ReadOnlyStringWrapper("${it.value.brand} (${it.value.model})")
            }
        })
        onDoubleClick {
            upload()
        }
        smartResize()
    }

    private val uploadButton = Button("Upload").apply {
        isDefaultButton = true;
        onAction = EventHandler {
            upload()
        }
    }

    private val bundleTextField = TextField()

    private val uploader = FileTransfer().apply {
        addFileTransferComponentListener(object : FileTransferEventListener {
            override fun onUpdateFtpServers(ftpServers: Set<FtpServer>) {
                ftpServersTableView.items.clear()
                ftpServersTableView.items.addAll(ftpServers)
            }

            override fun onUploadFileProgress(ftpServer: FtpServer?, file: File?, progress: Double) {
                if (progress == 1.0) {
                    progressBarDialog.close()
                } else {
                    progressBarDialog.updateProgress(progress)
                }
            }
        })
    }

    override fun onCreateComponent() {
        uploader.createUploader()
    }

    override fun onDeleteComponent() {
        uploader.destroyUploader()
    }

    init {
        title = "Connected Devices"
        initStyle(StageStyle.UTILITY)
        initOwner(mainStage)
        closeOnEscape()
        val contentPanel = GridPane().apply {
            padding = Insets(8.0)
            hgap = 8.0
            vgap = 8.0
        }
        contentPanel.add(ftpServersTableView, 0, 0)
        contentPanel.add(bundleTextField.withButton("...") {
            val file = FileChooser().apply {
                extensionFilters.add(FileChooser.ExtensionFilter("Application Bundle (*.bundle)", "*.bundle"))
            }.showOpenDialog(this)
            if (file != null && file.exists()) {
                bundleTextField.text = file.normalizedPath
            }
        }, 0, 1)
        contentPanel.add(fillWidth(right = listOf(uploadButton)), 0, 2)
        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        scene = Scene(contentPanel, 400.0, 400.0)
    }

    init {
        bundleTextField.text = studioPreferences.deviceChooserBundle
        bundleTextField.textProperty().onChange { text ->
            studioPreferences.edit {
                deviceChooserBundle = text ?: ""
            }
        }
    }

    private fun upload() {
        val ftpServer = ftpServersTableView.selectionModel.selectedItem ?: return
        val bundlePath = bundleTextField.text
        thread {
            uploader.uploadFile(ftpServer, File(bundlePath))
        }
        hide()
        progressBarDialog.showAndWait()
    }

    fun show(filePath: String) {
        show()
        bundleTextField.text = filePath
    }

}
