package featurea.examples.drawLines

import featurea.math.Point
import featurea.math.Vector2
import featurea.opengl.Buffer

fun Buffer.vertex(point: Point, normal: Vector2, alpha: Float) {
    vertex(point.x, point.y, normal.x, normal.y, alpha)
}
