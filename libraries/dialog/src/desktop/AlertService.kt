package featurea.dialog

import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.StageStyle

actual class AlertService actual constructor(override val module: Module) : Component {

    actual fun alert(title: String?, text: String, vararg buttons: String, complete: (button: String) -> Unit) =
        runOnJfxThread {
            val alert: Alert = Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.NO)
            alert.initStyle(StageStyle.UTILITY)
            alert.headerText = null
            alert.title = title
            alert.graphic = null
            alert.showAndWait()
            if (alert.result == ButtonType.YES) complete("Yes")
            if (alert.result == ButtonType.NO) complete("No")
        }

    actual fun toast(text: String) {
        // no op
    }

}
