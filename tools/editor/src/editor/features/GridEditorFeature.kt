package featurea.studio.editor.features

import featurea.calibrateByBase
import featurea.math.toSize
import featurea.math.toPoint
import featurea.content.ResourceTag
import featurea.runtime.Module
import featurea.studio.editor.EditorFeature
import featurea.studio.editor.components.positionKey
import featurea.studio.editor.components.sizeKey

private const val TOLERANCE = 1.0

class GridEditorFeature(module: Module) : EditorFeature(module) {

    override fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String = when (key) {
        editor.selectionService.positionKey -> {
            val position = value.toPoint()
            position.x = position.x.toDouble().calibrateByBase(TOLERANCE).toFloat()
            position.y = position.y.toDouble().calibrateByBase(TOLERANCE).toFloat()
            "${position.x}, ${position.y}"
        }
        editor.selectionService.sizeKey -> {
            val size = value.toSize()
            size.width = size.width.toDouble().calibrateByBase(TOLERANCE).toFloat()
            size.height = size.height.toDouble().calibrateByBase(TOLERANCE).toFloat()
            "${size.width}, ${size.height}"
        }
        else -> value
    }

}
