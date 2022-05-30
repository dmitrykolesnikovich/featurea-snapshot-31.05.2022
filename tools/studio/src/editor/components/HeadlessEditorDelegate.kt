package featurea.studio.editor.components

import featurea.content.ResourceTag
import featurea.layout.Camera
import featurea.math.Rectangle
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.studio.editor.EditorDelegate

class HeadlessEditorDelegate(override val module: Module) : Component, EditorDelegate {
    override val isHeadless: Boolean = true
    override val camera: Camera get() = error("stub")
    override fun selectionOf(rmlTag: ResourceTag): Selection = Selection(module).init(rmlTag)
    override fun selectionOf(component: Any): Selection = error("stub")
    override fun select(globalX: Float, globalY: Float): Selection = error("stub")
    override fun select(localRectangle: Rectangle): List<Selection> = error("stub")
    override fun immediateBoundsOf(component: Any): Rectangle = error("stub")
}
