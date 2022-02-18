package featurea.studio

import featurea.firstStringOrNull
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.script.Script
import featurea.splitAndTrim
import featurea.studio.editor.components.ColorChooser
import featurea.studio.home.components.FileChooserDialog
import javafx.stage.Stage

class EditorDocket(override val module: Module) : Component, Script {

    private val colorChooser: ColorChooser = import()

    override suspend fun executeAction(action: String, args: List<Any?>, isSuper: Boolean): Any {
        val value: String? = args.firstStringOrNull()
        return when (action) {
            "ColorChooser.chooseColor" -> colorChooser.chooseColor(checkNotNull(value))
            else -> Unit
        }
    }

}

class Docket(override val module: Module) : Component, Script {

    private val fileChooser: FileChooserDialog = import()

    override suspend fun executeAction(action: String, args: List<Any?>, isSuper: Boolean): Any {
        return when (action) {
            "FileChooserDialog.chooseFile" -> {
                val resourcePath: String = args[0] as String
                val extensions: List<String> = args[1].toString().splitAndTrim(",")
                val stage: Stage? = args.getOrNull(2) as? Stage
                fileChooser.chooseFile(resourcePath, extensions, stage)
            }
            else -> Unit
        }
    }

}
