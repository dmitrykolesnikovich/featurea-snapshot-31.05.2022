package featurea.layout

import featurea.math.Size
import featurea.math.min

// todo refactor
internal fun CameraStyle.layoutCamera(size: Size) {

    fun center(isVertical: Boolean, size: Size) {
        val surface = camera.surface
        val tx: Float
        val ty: Float
        if (isVertical) {
            tx = 0f
            val newCameraHeight = surface.size.height * surface.transform.sy
            ty = (size.height - newCameraHeight) / 2
            surface.viewport.width = size.width
            surface.viewport.height = newCameraHeight
        } else {
            val newCameraWidth = surface.size.width * surface.transform.sx
            tx = (size.width - newCameraWidth) / 2
            ty = 0f
            surface.viewport.width = newCameraWidth
            surface.viewport.height = size.height
        }
        surface.transform.edit { assignTranslation(tx, ty) } // quickfix todo conceptualize
    }

    fun anchor(isVertical: Boolean, size: Size) {
        if (isVertical) {
            vertical?.layout?.invoke(camera, size)
        } else {
            horizontal?.layout?.invoke(camera, size)
        }
    }

    val surface = camera.surface
    surface.transform.edit { assignOrigin(surface.origin) } // quickfix todo conceptualize
    if (size.isNotEmpty() && surface.size.isNotEmpty()) {
        val widthRatio: Float = size.width / surface.size.width
        val heightRatio: Float = size.height / surface.size.height
        val minRatio: Float = min(widthRatio, heightRatio)
        surface.transform.edit { assignScale(minRatio, minRatio) } // quickfix todo conceptualize
        val isVertical: Boolean = widthRatio == minRatio
        center(isVertical, size)
        anchor(isVertical, size)
    }
}
