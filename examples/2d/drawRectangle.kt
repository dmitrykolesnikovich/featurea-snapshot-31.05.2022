package featurea.examples.drawLines

import featurea.ApplicationDelegate
import featurea.utils.Colors
import featurea.examples.drawLines.Resources.drawRectangleShader
import featurea.math.*
import featurea.opengl.Buffer
import featurea.opengl.Program

fun drawRectangle() = bootstrapTest {
    val program: Program = shaderContent.findProgram(drawRectangleShader)
    val buffer: Buffer = program.createBuffer(drawCallLimit = 1, verticesPerDraw = 4)
    val size: Size = Size(2, 120)
    val origin: Point = Point(100f, 200f)
    val rotationMatrix: Matrix = Matrix()
    val rotationAngle: Angle = Angle()

    app.repeatOnUpdate { elapsedTime ->
        gl.clear(Colors.orangeColor)
        program.enable()
        program.uniforms["PROJECTION_MATRIX"] = projectionMatrix
        program.uniforms["WINDOW_SIZE"] = window.surface.size
        program.uniforms["ORIGIN"] = origin
        program.uniforms["SIZE"] = size
        program.uniforms["ROTATION"] = rotationMatrix.rotate(origin.x, origin.y, rotationAngle.assign(elapsedTime / 1000.0 * 10.0))
        gl.drawTriangleFan(buffer)
        program.disable()
    }
    app.delegate = object : ApplicationDelegate {
        override fun resize(width: Int, height: Int) {
            buffer.clear()
            buffer.apply {
                vertex(0f, 0f)
                vertex(0f, height.toFloat())
                vertex(width.toFloat(), height.toFloat())
                vertex(width.toFloat(), 0f)
            }
        }
    }
}
