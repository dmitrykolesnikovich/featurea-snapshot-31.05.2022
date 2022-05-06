package featurea.examples.drawLines

import featurea.ApplicationDelegate
import featurea.utils.Colors.whiteColor
import featurea.examples.drawLines.Resources.drawLineShader
import featurea.math.Point
import featurea.math.Vector2
import featurea.math.Vector3
import featurea.opengl.Buffer
import featurea.opengl.Program

fun test1() = bootstrapTest {
    val program: Program = shaderContent.findProgram(drawLineShader)
    val buffer: Buffer = program.createBuffer(drawCallLimit = 0, verticesPerDraw = 12)
    val mouse: Point = Point(200f, 25f)
    val points = listOf(
        Point(5f, 5f),
        mouse,
        Point(400f, 425f),
    )
    buffer.increaseDrawCallLimit(points.size - 1)

    input.addListener { event ->
        mouse.assign(event.x1, event.y1)
        window.invalidate()
    }

    app.repeatOnUpdate {
        gl.clear(whiteColor)
        program.enable()
        program.uniforms["HALF_WIDTH"] = 1f
        program.uniforms["PROJECTION_MATRIX"] = projectionMatrix
        program.uniforms["TINT"] = Vector3(1f, 0.5f, 0f)
        gl.drawTriangleStrip(buffer)
        program.disable()
    }

    app.delegate = object : ApplicationDelegate {
        override fun invalidate() {
            buffer.clear()
            for(index in 0..1) {
                val p1 = points[index]
                val p2 = points[index + 1]
                val dx: Float = p2.x - p1.x
                val dy: Float = p2.y - p1.y
                val n1: Vector2 = Vector2(dy, -dx).normalize()
                val n2: Vector2 = Vector2(-dy, dx).normalize()
                val n3: Vector2 = n1 * 2.5f
                val n4: Vector2 = n2 * 2.5f
                buffer.apply {
                    vertex(p1, n1, 1f) // 1
                    vertex(p1, n2, 1f) // 2
                    vertex(p2, n1, 1f) // 3
                    vertex(p2, n2, 1f) // 4

                    vertex(p1, n3, 0f)
                    vertex(p1, n1, 1f) // 1
                    vertex(p2, n3, 0f)
                    vertex(p2, n1, 1f) // 3

                    vertex(p1, n2, 1f) // 2
                    vertex(p1, n4, 0f)
                    vertex(p2, n2, 1f) // 4
                    vertex(p2, n4, 0f)
                }
            }
        }
    }

}
