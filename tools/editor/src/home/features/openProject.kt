package featurea.studio.home.features

import featurea.desktop.MainStageProxy
import featurea.desktop.Preferences
import featurea.desktop.jfx.FileChooser
import featurea.jvm.normalizedPath
import featurea.runtime.Action
import featurea.runtime.import
import featurea.utils.splitAndTrim
import featurea.studio.home.StudioDelegate
import featurea.studio.home.StudioPanel
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import java.io.File

val openProject: Action = {
    val delegate: StudioDelegate = import<StudioPanel>().options.delegate
    val mainStage: Stage = import(MainStageProxy)
    val recentProjectsPreferences: Preferences = Preferences("recentProjects")
    val fileChooser: FileChooser = FileChooser(ExtensionFilter("Project files (*.project)", "*.project"))
    val file: File? = fileChooser.showOpenDialog(mainStage)
    if (file != null && file.exists()) {
        delegate.openProject(file)
        recentProjectsPreferences.edit {
            this["list", ""].splitAndTrim(",").add(file.normalizedPath)
        }
    }
}
