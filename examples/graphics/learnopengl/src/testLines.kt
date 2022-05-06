package featurea.examples.learnopengl

import featurea.utils.Colors.redColor
import featurea.opengl.*
import featurea.shader.compile
import featurea.examples.learnopengl.Resources.testLinesShader

fun testLines() = bootstrapTest {
    lateinit var buffer: Buffer
    lateinit var program: Program

    load {
        buffer = gl.createBuffer(0, 0)
        gl.bindBufferData(buffer, data = floatArrayOf(-0.5f, 0.5f, 0.5f, 0.5f, 0f, -0.9f), usage = STATIC_DRAW)
        program = gl.createProgram()
        program.compile(testLinesShader)
    }

    update {
        gl.clearColorBuffer(0.2f, 0.3f, 0.3f, 1.0f)
        gl.useProgram(program)
        gl.bindBuffer(ARRAY_BUFFER, buffer)
        program.enableAttributes()
        program.uniforms["tint"] = redColor
        gl.enable(LINE_SMOOTH)
        gl.drawArrays(LINE_LOOP, 0, 3)
    }

}
