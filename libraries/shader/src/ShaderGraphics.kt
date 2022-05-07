package featurea.shader

import featurea.ApplicationComponent
import featurea.layout.Camera
import featurea.layout.coordinates
import featurea.math.*
import featurea.opengl.*
import featurea.runtime.import
import featurea.window.Window

open class ShaderGraphics(val shader: String) : ApplicationComponent() {

    val gl: Opengl = import(OpenglProxy)
    private val shaderContent: ShaderContent = import()
    private val program: Program = shaderContent.findProgram(shader)
    internal val window: Window = import()

    val uniforms: Uniforms get() = program.uniforms
    val modelMatrix: Matrix = Matrix()
    val viewMatrix: Matrix = Matrix()
    val projectionMatrix: Matrix = Matrix()
    internal val viewProjectionMatrix: Matrix = Matrix()
    internal val viewScale: Vector2 = Vector2(1f, 1f)

    fun enable() {
        program.enable()
        uniforms["MODEL_MATRIX"] = modelMatrix
        uniforms["VIEW_MATRIX"] = viewMatrix
        uniforms["PROJECTION_MATRIX"] = projectionMatrix
        uniforms["VIEW_PROJECTION_MATRIX"] = viewProjectionMatrix
        uniforms["VIEW_SCALE"] = viewScale
        uniforms["WINDOW_SIZE"] = window.surface.size
    }

    fun disable() {
        program.disable()
    }

    open fun resize(camera: Camera) {
        val coordinates: Coordinates = if (window.useCamera) camera.coordinates else window.surface.coordinates
        projectionMatrix.assign(window.surface.matrix).translate(window.surface.origin)
        val transform: Transform = coordinates()
        viewMatrix.assign(transform.matrix)
        viewProjectionMatrix.assignMultiplication(projectionMatrix, viewMatrix)
        viewScale.assign(transform.sx, transform.sy)
    }

    fun Buffer(drawCallLimit: Int, verticesPerDraw: Int, isMedium: Boolean = false): Buffer {
        return program.createBuffer(drawCallLimit, verticesPerDraw, isMedium)
    }

}

fun ShaderGraphics.mv(point: Point): Point = mv(point.x, point.y)

fun ShaderGraphics.mv(x: Double, y: Double): Point = mv(x.toFloat(), y.toFloat())

fun ShaderGraphics.mv(x: Float, y: Float): Point = (viewMatrix * (modelMatrix * Pool.point0.assign(x, y)))

fun ShaderGraphics.mvp(point: Point): Point = mvp(point.x, point.y)

fun ShaderGraphics.mvp(x: Double, y: Double): Point = mvp(x.toFloat(), y.toFloat())

fun ShaderGraphics.mvp(x: Float, y: Float): Point = (viewProjectionMatrix * (modelMatrix * Pool.point1.assign(x, y)))

fun ShaderGraphics.mvpDiff(vector: Vector2, result: Vector2): Vector2 = mvpDiff(vector.x, vector.y, result)

fun ShaderGraphics.mvpDiff(x: Float, y: Float, result: Vector2): Vector2 {
    val xm: Float = if (x == 0f) 0f else (x / window.surface.viewport.width) * 2 * viewScale.x
    val ym: Float = if (y == 0f) 0f else (y / window.surface.viewport.height) * 2 * viewScale.y
    result.assign(xm, ym)
    return result
}

/*internals*/

private object Pool {
    val point0: Point get() = POINTS[0].assign(0f, 0f)
    val point1: Point get() = POINTS[1].assign(0f, 0f)
    private val POINTS: Array<Point> = Array(size = 2) { Point() }
}
