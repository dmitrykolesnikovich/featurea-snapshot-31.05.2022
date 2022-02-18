package featurea.examples.drawLines

import featurea.Colors.orangeColor
import featurea.examples.drawLines.Resources.tutorialShader
import featurea.opengl.Buffer
import featurea.opengl.Program

fun tutorial() = bootstrapTest {
    val program: Program = shaderContent.findProgram(tutorialShader)
    val buffer: Buffer = program.createBuffer(drawCallLimit = 1, verticesPerDraw = 4)
    buffer.apply {
        vertex(40f, 20f, 40f, 20f, 90f, 90f, 1f)
        vertex(40f, 20f, 90f, 90f, 220f, 40f, -1f)
        vertex(90f, 90f, 220f, 40f, 320f, 200f, 1f)
        vertex(220f, 40f, 320f, 200f, 420f, 200f, -1f)
    }
    app.repeatOnUpdate {
        gl.clear(orangeColor)
        program.enable()
        program.uniforms["PROJECTION_MATRIX"] = projectionMatrix
        program.uniforms.set("settings", 2f, 1f, 2f, 1f)
        program.uniforms.set("tint", 0f, 0f, 0f, 1f)
        gl.drawTriangleStrip(buffer)
        program.disable()
    }
}
