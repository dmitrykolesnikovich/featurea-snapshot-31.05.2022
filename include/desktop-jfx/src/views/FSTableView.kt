package featurea.desktop.jfx

import javafx.scene.control.TableView

open class FSTableView<T> : TableView<T>() {

    private var transparentStyle: String = ""

    init {
        editableProperty().set(true)
        stylesheets.addAll("featurea/jfx/hidden-tableview-headers.css".externalPath)
        columnResizePolicy = SmartResize.POLICY
    }

    var gridLinesVisible: Boolean = true
        set(value) {
            field = value
            if (value) {
                transparentStyle.replace("-fx-table-cell-border-color: transparent;", "")
            } else {
                transparentStyle += "-fx-table-cell-border-color: transparent;"
            }
            style = transparentStyle
        }

}
