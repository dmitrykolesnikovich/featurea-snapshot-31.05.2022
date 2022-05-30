package featurea.studio.editor

import featurea.content.ResourceTag
import featurea.layout.Camera
import featurea.math.Rectangle
import featurea.studio.editor.components.Selection

interface EditorDelegate {
    val isHeadless: Boolean
    val camera: Camera
    fun selectionOf(rmlTag: ResourceTag): Selection?
    fun selectionOf(component: Any): Selection
    fun select(globalX: Float, globalY: Float): Selection?
    fun select(localRectangle: Rectangle): List<Selection>
    fun immediateBoundsOf(component: Any): Rectangle
}
