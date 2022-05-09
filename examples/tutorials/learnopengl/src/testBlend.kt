package featurea.examples.learnopengl

import featurea.utils.log
import featurea.opengl.*
import featurea.examples.learnopengl.Resources.imageShader

fun testBlend() = bootstrapTest {
    val vertices: FloatArray = floatArrayOf(
        // positions  // texture coordinates
        +0.9f, +0.9f, 0.0f, 1.0f, 1.0f, // top right
        +0.9f, -0.9f, 0.0f, 1.0f, 0.0f, // bottom right
        -0.9f, -0.9f, 0.0f, 0.0f, 0.0f, // bottom left
        +0.9f, +0.9f, 0.0f, 1.0f, 1.0f, // top right
        -0.9f, -0.9f, 0.0f, 0.0f, 0.0f, // bottom left
        -0.9f, +0.9f, 0.0f, 0.0f, 1.0f, // top left
    )
    lateinit var buffer: Buffer
    lateinit var program: Program
    lateinit var diffuseTexture: Texture

    load {
        diffuseTexture = imageContent.findTexture(test.diffuseTexturePath)
        val (vertexShaderSource, pixelShaderSource) = shaderContent.readShaderSource(imageShader)

        buffer = gl.createBuffer(0, 0) // quickfix todo improve
        gl.bindBuffer(ARRAY_BUFFER, buffer)
        gl.bufferData(ARRAY_BUFFER, vertices, STATIC_DRAW)

        val vertexShader: Shader = gl.createShader(VERTEX_SHADER)
        gl.shaderSource(vertexShader, vertexShaderSource)
        gl.compileShader(vertexShader)
        if (gl.getShaderParameter(vertexShader, COMPILE_STATUS) != TRUE) log(gl.getShaderInfoLog(vertexShader))

        val pixelShader: Shader = gl.createShader(FRAGMENT_SHADER)
        gl.shaderSource(pixelShader, pixelShaderSource)
        gl.compileShader(pixelShader)
        if (gl.getShaderParameter(pixelShader, COMPILE_STATUS) != TRUE) log(gl.getShaderInfoLog(pixelShader))

        program = gl.createProgram()
        gl.attachShader(program, vertexShader)
        gl.attachShader(program, pixelShader)
        gl.linkProgram(program)
        if (gl.getProgramParameter(program, LINK_STATUS) != TRUE) log(gl.getProgramInfoLog(program))

        gl.deleteShader(vertexShader)
        gl.deleteShader(pixelShader)
    }

    update {
        gl.clearColor(0f, 0f, 0f, 1f)
        gl.clear(COLOR_BUFFER_BIT)

        val (r, g, b, a) = test.blendFunction.color
        gl.blendColor(r, g, b, a)
        gl.blendFunction(test.blendFunction.sourceFactor, test.blendFunction.destinationFactor)

        gl.useProgram(program)

        gl.activeTexture(TEXTURE0)
        gl.bindTexture(TEXTURE_2D, diffuseTexture)
        val sampling: Sampling = test.sampling
        gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_S, sampling.wrappingFunction.first)
        gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_T, sampling.wrappingFunction.second)
        gl.textureParameter(TEXTURE_2D, TEXTURE_MIN_FILTER, test.textureFilter.minFilter)
        gl.textureParameter(TEXTURE_2D, TEXTURE_MAG_FILTER, test.textureFilter.magFilter)
        gl.uniform(gl.getUniformLocation(program, "ourTexture"), 0)

        gl.uniform(gl.getUniformLocation(program, "alphaTest"), test.alphaTest)
        gl.uniform(gl.getUniformLocation(program, "alphaTest2"), test.alphaTest2)

        gl.bindBuffer(ARRAY_BUFFER, buffer)

        gl.enableVertexAttribArray(0)
        gl.vertexAttributePointer(0, 3, FLOAT, 5 * Float.SIZE_BYTES, 0 * Float.SIZE_BYTES)

        gl.enableVertexAttribArray(1)
        gl.vertexAttributePointer(1, 2, FLOAT, 5 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)

        gl.drawArrays(TRIANGLES, 0, 6)
    }

}
