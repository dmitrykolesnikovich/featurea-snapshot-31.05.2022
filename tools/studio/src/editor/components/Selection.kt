package featurea.studio.editor.components

import featurea.content.ResourceTag
import featurea.math.Rectangle
import featurea.math.Vector2
import featurea.runtime.*
import featurea.studio.editor.Editor
import featurea.studio.editor.findAttributeKeys
import featurea.window.Window
import featurea.window.toGlobalCoordinates

class Selection(override val module: Module) : Component {

    val editor: Editor = import()

    lateinit var rmlTag: ResourceTag
    lateinit var instance: Any

    // >> quickfix todo decouple graphics
    val window: Window by lazy { import() }
    lateinit var localRectangle: Rectangle
    private val vectorResult: Vector2.Result = Vector2().Result()
    // <<

    fun init(rmlTag: ResourceTag): Selection {
        this.rmlTag = rmlTag
        return this
    }

    fun init(rmlTag: ResourceTag, instance: Any, localRectangle: Rectangle): Selection {
        this.rmlTag = rmlTag
        this.instance = instance
        this.localRectangle = localRectangle
        return this
    }

    private val globalRectangleResult = Rectangle()
    val globalRectangle: Rectangle
        get() {
            val (_x1, _y1, _x2, _y2) = localRectangle
            val (x1, y1) = window.toGlobalCoordinates(editor.delegate.camera, _x1, _y1, vectorResult)
            val (x2, y2) = window.toGlobalCoordinates(editor.delegate.camera, _x2, _y2, vectorResult)
            return globalRectangleResult.assign(x1, y1, x2, y2)
        }

    val rectangle: Rectangle
        get() {
            return if (editor.window.useCamera) globalRectangle else localRectangle
        }

    override fun equals(other: Any?): Boolean {
        return if (other is Selection) other.rmlTag === rmlTag else false
    }

    override fun hashCode(): Int {
        return instance.hashCode()
    }

}

// constructor
fun Component.Selection(init: Selection.() -> Unit = {}): Selection = create(init)

/*convenience*/

fun Selection.isMovable(): Boolean {
    if (rmlTag === editor.tab.rmlResource.rmlTag) return false // quickfix todo improve
    return rmlTag.isMovable(editor)
}

fun Selection.isNotMovable(): Boolean {
    return !isMovable()
}

fun ResourceTag.isMovable(editor: Editor): Boolean {
    val rmlTag: ResourceTag = this
    if (rmlTag.attributes.contains("position") && rmlTag.attributes.contains("size")) {
        return true
    }
    val keys: List<String> = editor.findAttributeKeys(rmlTag)
    return keys.contains("position") && keys.contains("size")
}

fun ResourceTag.isNotMovable(editor: Editor): Boolean {
    return !isMovable(editor)
}
