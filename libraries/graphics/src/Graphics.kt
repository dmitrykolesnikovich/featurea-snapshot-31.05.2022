package featurea.graphics

import featurea.utils.Color
import featurea.utils.Colors
import featurea.app.applicationScope
import featurea.graphics.Resources.defaultShader
import featurea.layout.Camera
import featurea.math.Line
import featurea.math.Rectangle
import featurea.opengl.Buffer
import featurea.runtime.Component
import featurea.shader.ShaderGraphics

class Graphics : ShaderGraphics(defaultShader) {

    var lineWidth: Float = 1f
    val lines: Buffer = Buffer(drawCallLimit = 0, verticesPerDraw = 2)
    var linesLimit: Int by lines.limit
    val rectangles: Buffer = Buffer(drawCallLimit = 0, verticesPerDraw = 8)
    var rectanglesLimit: Int by rectangles.limit
    private val bufferBuilder: BufferBuilder = BufferBuilder()
    var isValid: Boolean = false
        private set

    fun buffer(setup: BufferBuilder.() -> Unit) {
        bufferBuilder.setup()
    }

    fun draw(camera: Camera) {
        if (!isValid) {
            resize(camera)
            isValid = true
        }
        enable()
        gl.lineWidth(lineWidth)
        if (lines.isNotEmpty) {
            gl.drawLines(lines)
        }
        if (rectangles.isNotEmpty) {
            gl.drawLines(rectangles)
        }
        disable()
    }

    fun invalidate() {
        isValid = false
        lines.clear()
        rectangles.clear()
    }

    fun destroy() {
        gl.deleteBuffer(lines)
        gl.deleteBuffer(rectangles)
    }

    inner class BufferBuilder {

        fun Line(line: Line, color: Color = Colors.whiteColor) {
            val (x1, y1, x2, y2) = line
            Line(x1, y1, x2, y2, color)
        }

        fun Line(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = Colors.whiteColor) {
            val (r, g, b, a) = color
            lines.apply {
                vertex(x1, y1, r, g, b, a)
                vertex(x2, y2, r, g, b, a)
            }
        }

        fun Rectangle(rectangle: Rectangle, color: Color = Colors.whiteColor) {
            val (x1, y1, x2, y2) = rectangle
            Rectangle(x1, y1, x2, y2, color)
        }

        fun Rectangle(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = Colors.whiteColor) {
            val (r, g, b, a) = color
            Rectangle(x1, y1, x2, y2, r, g, b, a)
        }

        fun Rectangle(x1: Float, y1: Float, x2: Float, y2: Float, r: Float, g: Float, b: Float, a: Float) {
            rectangles.apply {
                vertex(x1, y1, r, g, b, a)
                vertex(x2, y1, r, g, b, a)
                vertex(x2, y1, r, g, b, a)
                vertex(x2, y2, r, g, b, a)
                vertex(x2, y2, r, g, b, a)
                vertex(x1, y2, r, g, b, a)
                vertex(x1, y2, r, g, b, a)
                vertex(x1, y1, r, g, b, a)
            }
        }

    }

}

// constructor
fun Component.Graphics(init: Graphics.() -> Unit = {}): Graphics = applicationScope {
    featurea.graphics.Graphics().apply(init)
}
