package featurea.window

import featurea.desktop.MainStageProxy
import featurea.math.Size
import featurea.runtime.Action
import featurea.runtime.import
import featurea.utils.watchOnEditorThread
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import kotlin.math.pow

val initWindow: Action = {
    val mainStage: Stage = import(MainStageProxy)
    val window: Window = import()
    window.titleProperty.watchOnEditorThread {
        mainStage.title = window.title
    }
    window.sizeProperty.watchOnEditorThread {
        mainStage.width = window.size.width.toDouble()
        mainStage.height = window.size.height.toDouble()
    }
}

// 1440x900 -> 600
fun defaultSide(): Int {
    val bounds: Rectangle2D = Screen.getPrimary().bounds
    return (bounds.width * bounds.height).pow(1 / 2.2).toInt()
}

fun defaultSize(): Size {
    val side: Int = defaultSide()
    return Size(side, side)
}
