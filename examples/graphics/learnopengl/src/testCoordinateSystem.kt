package featurea.examples.learnopengl

import featurea.image.Image
import featurea.log
import featurea.math.Matrix
import featurea.math.Vector
import featurea.mto
import featurea.opengl.*
import featurea.shader.reader.ShaderSource
import featurea.shader.reader.offsetOf
import featurea.window.aspectRatio
import featurea.examples.learnopengl.Resources.containerPng
import featurea.examples.learnopengl.Resources.test3dShader

fun testCoordinateSystem() = bootstrapTest {
    val modelMatrix: Matrix = Matrix()
    val viewMatrix: Matrix = Matrix()
    val projectionMatrix: Matrix = Matrix()
    lateinit var buffer: Buffer
    lateinit var program: Program

    loader.loadResource(containerPng)

    init {
        diffuseTexturePath = containerPng
        sampling.wrappingFunction = CLAMP_TO_EDGE mto CLAMP_TO_EDGE
        rotationAngle = 0.0
        rotationAxis = Vector(1.5f, 4.5f, -10f)
        rotationIncrement = 2f
        viewTranslation = Vector(0f, 0f, -2.5f)
        near = 0.1f
        far = 100f
        fov = 45f
    }

    load {
        // vertices
        buffer = gl.createBuffer(0, 0)
        gl.bindBuffer(ARRAY_BUFFER, buffer)
        gl.bufferData(ARRAY_BUFFER, cubeVerticesWithUv, STATIC_DRAW)

        // shader
        val source: ShaderSource = shaderContent.readShaderSource(test3dShader)

        val vertexShader: Shader = gl.createShader(VERTEX_SHADER)
        gl.shaderSource(vertexShader, source.vertexShaderSource)
        gl.compileShader(vertexShader)
        if (gl.getShaderParameter(vertexShader, COMPILE_STATUS) != TRUE) log(gl.getShaderInfoLog(vertexShader))

        val pixelShader: Shader = gl.createShader(FRAGMENT_SHADER)
        gl.shaderSource(pixelShader, source.pixelShaderSource)
        gl.compileShader(pixelShader)
        if (gl.getShaderParameter(pixelShader, COMPILE_STATUS) != TRUE) log(gl.getShaderInfoLog(pixelShader))

        program = gl.createProgram()
        gl.attachShader(program, vertexShader)
        gl.attachShader(program, pixelShader)
        gl.linkProgram(program)
        if (gl.getProgramParameter(program, LINK_STATUS) != TRUE) log(gl.getProgramInfoLog(program))

        for (attribute in source.attributes) {
            val location: Int = gl.getAttributeLocation(program, attribute.name)
            check(location != -1)
            attribute.location = location
            attribute.offset = source.attributes.offsetOf(location)
        }
        program.attributes.init(source.attributes)

        gl.deleteShader(vertexShader)
        gl.deleteShader(pixelShader)
    }

    resize {
        projectionMatrix.assignPerspective(test.near, test.far, test.fov, window.aspectRatio)
    }

    update { elapsedTime: Float ->
        gl.clearColor(0.2f, 0.3f, 0.3f, 1.0f)
        gl.enable(DEPTH_TEST)
        gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)

        gl.useProgram(program)

        val diffuseTexturePath: String? = test.diffuseTexturePath
        if (diffuseTexturePath != null) {
            val diffuseImage: Image = imageContent.findImage(diffuseTexturePath)

            // glsl: texture -> slot 0
            gl.uniform(gl.getUniformLocation(program, "texture"), 0) // texture -> slot 0
            // kotlin: texture -> slot 0
            gl.activeTexture(TEXTURE0) // -> slot 0
            gl.bindTexture(TEXTURE_2D, diffuseImage.texture) // texture ->

            val sampling: Sampling = test.sampling
            gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_S, sampling.wrappingFunction.first)
            gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_T, sampling.wrappingFunction.second)
            gl.textureParameter(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
            gl.textureParameter(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
        }

        test.rotationAngle += test.rotationIncrement * elapsedTime / 1_000f
        modelMatrix.assignIdentity().rotate(test.rotationAxis, test.rotationAngle)
        viewMatrix.assignTranslation(test.viewTranslation)
        program.uniforms["model"] = modelMatrix
        program.uniforms["view"] = viewMatrix
        program.uniforms["projection"] = projectionMatrix

        gl.bindBuffer(ARRAY_BUFFER, buffer)
        for (attribute in program.attributes) {
            gl.enableVertexAttribArray(attribute.location)
            gl.vertexAttributePointer(
                index = attribute.location,
                size = attribute.size,
                type = FLOAT,
                stride = program.attributes.vertexSizeInBytes,
                offset = attribute.offset
            )
        }

        gl.drawArrays(TRIANGLES, 0, 6)

        gl.bindTexture(TEXTURE_2D, null)
        gl.activeTexture(TEXTURE1) // disable texture slot
        gl.bindBuffer(ELEMENT_ARRAY_BUFFER, null)
        gl.bindBuffer(ARRAY_BUFFER, null)
    }

}
