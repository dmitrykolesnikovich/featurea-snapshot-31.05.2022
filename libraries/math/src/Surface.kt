package featurea.math

class Surface {

    val origin: Point = Point()
    val size: Size = Size()
    val transform: Transform = Transform()
    val coordinates: Coordinates = { transform }
    val viewport: Size = Size()
    val matrix: Matrix = Matrix()

    val left: Float get() = origin.x
    val top: Float get() = origin.y
    val right: Float get() = origin.x + size.width
    val bottom: Float get() = origin.y + size.height

    fun assign(original: Surface) {
        origin.assign(original.origin)
        size.assign(original.size)
        transform.edit { assign(original.transform) }
        viewport.assign(original.viewport)
        matrix.assign(original.matrix)
    }

}
