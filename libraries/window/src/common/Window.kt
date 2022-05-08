package featurea.window

import featurea.app.Application
import featurea.app.DefaultApplicationDelegate
import featurea.layout.Camera
import featurea.layout.Layout
import featurea.layout.Orientation
import featurea.layout.View
import featurea.loader.LoaderController
import featurea.math.*
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.Property
import featurea.utils.forEachEvent
import featurea.utils.replaceWith
import kotlin.reflect.KClass

// fixme: synchronize Window.size and Window.surface.size
class Window(override val module: Module) : Component {

    var layout: Layout = Layout()
    lateinit var orientation: Orientation
    private var oldOrientation: Orientation? = null
    val elements = mutableMapOf<View, WindowElement>()
    internal val elementProviders = mutableMapOf<KClass<*>, WindowElementProvider<View>>()
    val listeners = mutableListOf<WindowListener>()
    val surface: Surface = Surface()
    val sizeProperty = Property<Size>()
    var size: Size by sizeProperty
    val titleProperty = Property<String?>()
    var title: String? by titleProperty
    var useCamera: Boolean = true
    private val views: MutableSet<View> = linkedSetOf()

    // import
    private val app: Application = import()
    private val delegate: WindowDelegate = import()
    private val loaderController: LoaderController = import()

    init {
        app.delegateProperty.watch {
            invalidate()
        }
    }

    fun init() {
        listeners.forEachEvent {
            it.init()
        }
    }

    // todo eliminate suspend
    suspend fun update(elapsedTime: Float) {
        /*log("[Window] update: entering")*/
        app.elapsedTime = if (app.isEnable) elapsedTime else 0f // quickfix todo improve
        /*log("[Window] update: updateControllers")*/
        app.updateControllers()
        /*log("[Window] update: updateDelegate")*/
        if (!loaderController.isActive) {
            app.updateDelegate()
        }
        /*log("[Window] update: updateTasks")*/
        app.updateTasks()
    }

    fun resize(width: Int, height: Int) {
        surface.size.assign(width, height)
        listeners.forEachEvent {
            it.resize(width, height)
        }
        app.delegate.resize(width, height)
    }

    fun invalidate() {
        if (loaderController.isActive) return // quickfix todo improve
        if (app.delegate is DefaultApplicationDelegate) return // quickfix todo improve
        listeners.forEachEvent {
            it.invalidate()
        }
        app.delegate.invalidate()
    }

    fun updateLayout() {
        val layoutViews = layout.views
        views.removeAll(layoutViews)
        for (view in views) {
            delegate.removeView(view)
        }
        views.replaceWith(layoutViews)
        for (view in views) {
            delegate.appendView(view)
        }
        listeners.forEachEvent {
            it.updateLayout(layout)
        }
    }

    fun updateOrientation(newOrientation: Orientation) {
        if (oldOrientation != newOrientation) {
            oldOrientation = newOrientation
            listeners.forEachEvent {
                it.updateOrientation(newOrientation)
            }
        }
    }

    fun repeatOnInvalidate(task: () -> Unit) {
        listeners.add(WindowInvalidateListener(task))
    }

}

/*convenience*/

val Window.aspectRatio: Float get() = surface.size.aspectRatio

fun Window.toLocalRectangle(camera: Camera, rectangle: Rectangle, result: Vector2.Result): Rectangle {
    val (x1, y1) = toLocalCoordinates(camera, rectangle.x1, rectangle.y1, result)
    val (x2, y2) = toLocalCoordinates(camera, rectangle.x2, rectangle.y2, result)
    return Rectangle(x1, y1, x2, y2)
}

fun Window.toScissorRectangle(camera: Camera, rectangle: Rectangle, result: Vector2.Result): Rectangle.Result {
    return toScissorRectangle(camera, rectangle.x1, rectangle.y1, rectangle.x2, rectangle.y2, result)
}

fun Window.toScissorRectangle(camera: Camera, x1: Float, y1: Float, x2: Float, y2: Float, result: Vector2.Result): Rectangle.Result {
    val (x1, y1) = toGlobalCoordinates(camera, x1, y1, result)
    val (x2, y2) = toGlobalCoordinates(camera, x2, y2, result)
    return Rectangle(x1, y1, x2, y2).Result()
}

fun Window.notifyResize() {
    val (width, height) = surface.size
    resize(width.toInt(), height.toInt())
}
