package featurea.window

import featurea.layout.Camera
import featurea.layout.coordinates
import featurea.layout.x
import featurea.layout.y
import featurea.math.Size
import featurea.math.Transform
import featurea.math.Vector2

fun Camera.resize(window: Window) {
    resize(window.surface.viewport)
}

fun Window.toLocalCoordinates(camera: Camera, globalX: Float, globalY: Float, result: Vector2.Result): Vector2.Result {
    val transform: Transform = resolveTransform(camera)
    val cameraX: Float = if (useCamera) camera.x else 0f
    val cameraY: Float = if (useCamera) camera.y else 0f
    val localX: Float = (globalX - transform.tx) / transform.sx + cameraX
    val localY: Float = (globalY - transform.ty) / transform.sy + cameraY
    return result.apply(localX, localY)
}

fun Window.toGlobalCoordinates(camera: Camera, localX: Float, localY: Float, result: Vector2.Result): Vector2.Result {
    val transform: Transform = resolveTransform(camera)
    val cameraX: Float = if (useCamera) camera.x else 0f
    val cameraY: Float = if (useCamera) camera.y else 0f
    val globalX: Float = transform.tx + (localX - cameraX) * transform.sx
    val globalY: Float = transform.ty + (localY - cameraY) * transform.sy
    return result.apply(globalX, globalY)
}

fun Window.toGlobalWidth(camera: Camera, localWidth: Float): Float {
    val transform: Transform = resolveTransform(camera)
    val globalWidth: Float = localWidth * transform.sx
    return globalWidth
}

fun Window.toGlobalHeight(camera: Camera, localHeight: Float): Float {
    val transform: Transform = resolveTransform(camera)
    val globalHeight: Float = localHeight * transform.sy
    return globalHeight
}

fun Window.toGlobalDimensions(camera: Camera, lw: Float, lh: Float, result: Size.Result): Size.Result {
    val transform: Transform = resolveTransform(camera)
    val globalWidth: Float = lw * transform.sx
    val globalHeight: Float = lh * transform.sy
    return result.apply(globalWidth, globalHeight)
}

fun Window.toLocalDimensions(camera: Camera, gw: Float, gh: Float, result: Size.Result): Size.Result {
    val transform: Transform = resolveTransform(camera)
    val localWidth: Float = gw / transform.sx
    val localHeight: Float = gh / transform.sy
    return result.apply(localWidth, localHeight)
}

fun Window.resolveTransform(camera: Camera): Transform {
    val transform: Transform = if (useCamera) camera.coordinates() else surface.transform
    return transform
}
