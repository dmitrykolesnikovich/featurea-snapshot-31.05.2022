package featurea.studio.home.components

import featurea.System
import featurea.desktop.MainStageProxy
import featurea.jvm.normalizedPath
import featurea.jvm.findFileOrNull
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import javafx.stage.FileChooser
import javafx.stage.Stage

class FileChooserDialog(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val system: System = import()

    fun chooseFile(resourcePath: String, extensions: List<String> = emptyList(), stage: Stage?): String {
        val fileChooser = FileChooser().apply {
            extensionFilters.add(FileChooser.ExtensionFilter("${extensions.joinToString { it.toUpperCase() }} files", extensions.map { "*.${it}" }))
        }
        val file = system.findFileOrNull(resourcePath)
        fileChooser.initialDirectory = file?.parentFile
        fileChooser.initialFileName = file?.name
        val selectedFile = fileChooser.showOpenDialog(stage ?: mainStage)
        val result = selectedFile?.normalizedPath ?: resourcePath
        return result
    }

}
