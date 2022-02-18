package featurea.examples.webgl

import featurea.examples.webgl.Resources.test1Shader
import featurea.opengl.Buffer
import featurea.opengl.Program

fun test1() = bootstrapTest {
    val program: Program = shaderContent.findProgram(test1Shader)
    val buffer: Buffer = program.createBuffer(drawCallLimit = 1, verticesPerDraw = 4)
    buffer.apply {
        vertex(-1f, 1f)
        vertex(-1f, -1f)
        vertex(1f, -1f)
        vertex(1f, 1f)
    }
    app.repeatOnUpdate {
        program.enable()
        program.uniforms["viewport"] = window.surface.viewport
        gl.drawTriangleFan(buffer)
        program.disable()
    }
}
