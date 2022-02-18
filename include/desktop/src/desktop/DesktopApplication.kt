package featurea.desktop

import featurea.desktop.jfx.disableJfxLogger
import featurea.desktop.jfx.initDesktopTheme
import featurea.runtime.Provide
import featurea.runtime.provide
import javafx.application.Application
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.stage.Stage

@Provide(MainStageProxy::class)
class DesktopApplication : Application() {

    override fun start(primaryStage: Stage) {
        disableJfxLogger()
        primaryStage.initDesktopTheme()
        provide(MainStageProxy(primaryStage))
    }

}
