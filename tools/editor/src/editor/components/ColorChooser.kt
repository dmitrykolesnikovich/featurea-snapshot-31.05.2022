package featurea.studio.editor.components

import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.*
import featurea.desktop.toJfxColor
import featurea.isValidColorResource
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

private val whiteColorResource: String = Color.WHITE.toColorResource()

class ColorChooser(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)

    private val stage = Stage(mainStage).registerGlobalKeyEvents()
    private val colorPicker = FSColorPicker()
    var result: Color? = null

    init {
        stage.title = "Choose Color"
        val rootPanel: GridPane = GridPane()
        rootPanel.add(colorPicker, 0, 0)
        colorPicker.customColorProperty.onChange { color ->
            colorPicker.rgbaTextField.text = (color?.toColorResource() ?: whiteColorResource).replace("#", "")
        }
        rootPanel.add(HBox().apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(/*top = */0.0, /*right = */(1.25 + 0.3333).em, /*bottom = */1.25.em, /*left = */0.0)
            children.add(Button("OK").apply {
                onAction = EventHandler {
                    result = colorPicker.customColorProperty.get()
                    stage.hide()
                }
            })
        }, 0, 1)
        colorPicker.rgbaTextField.textProperty().onChange {
            val text = it ?: return@onChange
            val colorResource = "#${text}"
            if (colorResource.isValidColorResource()) {
                colorPicker.customColorProperty.set(colorResource.toJfxColor())
            }
        }
        stage.scene = Scene(rootPanel)
        stage.isResizable = false
        stage.initStyle(StageStyle.UTILITY)
        stage.scene.stylesheets.add("featurea/jfx/color-picker.css".externalPath)
        stage.sizeToScene()
    }

    fun chooseColor(resourcePath: String): String {
        val colorResource = if (resourcePath.isValidColorResource()) resourcePath else "#FFFFFFFF"
        colorPicker.rgbaTextField.text = colorResource.replace("#", "")
        val startColor = colorResource.toJfxColor()
        colorPicker.currentColorProperty.set(startColor)
        stage.showAndWait()
        return (result ?: startColor).toColorResource()
    }

}
