package featurea.examples.learnopengl

import featurea.input.InputEventType
import featurea.keyboard.KeyEvent
import featurea.keyboard.KeyEventSource
import featurea.keyboard.KeyEventType
import featurea.math.*
import featurea.opengl.*
import featurea.shader.compile
import featurea.examples.learnopengl.CameraMovement.*
import featurea.examples.learnopengl.Resources.containerPng
import featurea.examples.learnopengl.Resources.containerSpecularPng
import featurea.examples.learnopengl.Resources.objectShader
import kotlin.math.cos
import kotlin.math.sin

fun testColors() = bootstrapTest {
    /*
    emissionTexture = "images/containerEmission.jpg"
    emissionTexture?.also { loader.loadResource(it) }
    */
    val projectionMatrix: Matrix = Matrix()
    val camera: Camera = Camera(pos = Vector(0f, 0f, 3f))

    // val camFront: Vector get() = camera.Front
    val mouse: Point = Point(-1f, -1f)
    var totalTime: Float = 0f

    // val cubeModel = Matrix()
    lateinit var cubeBuffer: Buffer
    lateinit var cubeShaderProgram: Program

    val lightModel: Matrix = Matrix()
    val lightPos: Vector = Vector(1.2f, 1.0f, 2.0f)
    // val lightColor: Vector = Vector()
    lateinit var lightBuffer: Buffer
    lateinit var lightShaderProgram: Program

    loader.loadResource(containerPng)
    loader.loadResource(containerSpecularPng)

    init {
        diffuseTexturePath = containerPng
        specularTexturePath = containerSpecularPng
    }

    load {
        // cube
        cubeBuffer = gl.createBuffer(0, 0)
        gl.bindBuffer(ARRAY_BUFFER, cubeBuffer)
        gl.bufferData(ARRAY_BUFFER, cubeVerticesWithNormalsAndUv, STATIC_DRAW)
        cubeShaderProgram = gl.createProgram()
        cubeShaderProgram.compile(objectShader)

        // light
        lightBuffer = gl.createBuffer(0, 0)
        gl.bindBuffer(ARRAY_BUFFER, lightBuffer)
        gl.bufferData(ARRAY_BUFFER, cubeVertices, STATIC_DRAW)
        lightShaderProgram = gl.createProgram()
        lightShaderProgram.compile(Resources.lightShader)
    }

    update { elapsedTime: Float ->
        // setup
        totalTime += elapsedTime
        val camPos: Vector = camera.position
        val totalTimeInSeconds: Float = totalTime / 1_000f

        // move the light source around the scene over time using sin
        lightPos.x = 1.0f + sin(totalTimeInSeconds) * 2.0f
        lightPos.y = sin(totalTimeInSeconds / 2.0f) * 1.0f

        // background
        gl.enable(DEPTH_TEST)
        gl.clearColor(0.1f, 0.1f, 0.1f, 1.0f)
        gl.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)

        projectionMatrix.assignPerspective(test.near, test.far, camera.zoom, window.surface.size.aspectRatio)
        lightModel.assignTranslation(lightPos).scale(0.2f)

        /*
        lightColor.assign(sin(totalTimeInSeconds * 2f), sin(totalTimeInSeconds * 0.7f), sin(totalTimeInSeconds * 1.3f))
        val diffuse = lightColor * 0.5f
        val ambient = diffuse * 0.2f
        */

        // cube
        gl.useProgram(cubeShaderProgram)
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "projection"), projectionMatrix)
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "view"), camera.viewMatrix())
        // gl.uniformMatrix4fv(gl.getUniformLocation(cubeShader, "model"), false, cubeModel)
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "cameraPosition"), camPos.x, camPos.y, camPos.z)
        test.diffuseTexturePath?.also { cubeShaderProgram.bindTexture("material.diffuse", 0, it, test.sampling) }
        test.specularTexturePath?.also { cubeShaderProgram.bindTexture("material.specular", 1, it, test.sampling) }
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "material.shininess"), 32.0f)

        // directional light
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "dirLight.direction"), -0.2f, -1.0f, -0.3f)
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "dirLight.ambient"), 0.05f, 0.05f, 0.05f)
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "dirLight.diffuse"), 0.4f, 0.4f, 0.4f)
        gl.uniform(gl.getUniformLocation(cubeShaderProgram, "dirLight.specular"), 0.5f, 0.5f, 0.5f)
        // point light 1
        cubeShaderProgram.uniforms["pointLights[0].position"] = lightPositions[0]
        cubeShaderProgram.uniforms.set("pointLights[0].ambient", 0.05f, 0.05f, 0.05f)


        cubeShaderProgram.uniforms.set("pointLights[0].diffuse", 0.8f, 0.8f, 0.8f)
        cubeShaderProgram.uniforms.set("pointLights[0].specular", 1.0f, 1.0f, 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[0].constant", 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[0].linear", 0.09f)
        cubeShaderProgram.uniforms.set("pointLights[0].quadratic", 0.032f)
        // point light 2
        cubeShaderProgram.uniforms.set("pointLights[1].position", lightPositions[1])
        cubeShaderProgram.uniforms.set("pointLights[1].ambient", 0.05f, 0.05f, 0.05f)
        cubeShaderProgram.uniforms.set("pointLights[1].diffuse", 0.8f, 0.8f, 0.8f)
        cubeShaderProgram.uniforms.set("pointLights[1].specular", 1.0f, 1.0f, 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[1].constant", 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[1].linear", 0.09f)
        cubeShaderProgram.uniforms.set("pointLights[1].quadratic", 0.032f)
        // point light 3
        cubeShaderProgram.uniforms.set("pointLights[2].position", lightPositions[2])
        cubeShaderProgram.uniforms.set("pointLights[2].ambient", 0.05f, 0.05f, 0.05f)
        cubeShaderProgram.uniforms.set("pointLights[2].diffuse", 0.8f, 0.8f, 0.8f)
        cubeShaderProgram.uniforms.set("pointLights[2].specular", 1.0f, 1.0f, 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[2].constant", 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[2].linear", 0.09f)
        cubeShaderProgram.uniforms.set("pointLights[2].quadratic", 0.032f)
        // point light 4
        cubeShaderProgram.uniforms.set("pointLights[3].position", lightPositions[3])
        cubeShaderProgram.uniforms.set("pointLights[3].ambient", 0.05f, 0.05f, 0.05f)
        cubeShaderProgram.uniforms.set("pointLights[3].diffuse", 0.8f, 0.8f, 0.8f)
        cubeShaderProgram.uniforms.set("pointLights[3].specular", 1.0f, 1.0f, 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[3].constant", 1.0f)
        cubeShaderProgram.uniforms.set("pointLights[3].linear", 0.09f)
        cubeShaderProgram.uniforms.set("pointLights[3].quadratic", 0.032f)
        // spotLight
        cubeShaderProgram.uniforms.set("spotLight.position", camera.position)
        cubeShaderProgram.uniforms.set("spotLight.direction", camera.front)
        cubeShaderProgram.uniforms.set("spotLight.ambient", 0.0f, 0.0f, 0.0f)
        cubeShaderProgram.uniforms.set("spotLight.diffuse", 1.0f, 1.0f, 1.0f)
        cubeShaderProgram.uniforms.set("spotLight.specular", 1.0f, 1.0f, 1.0f)
        cubeShaderProgram.uniforms.set("spotLight.constant", 1.0f)
        cubeShaderProgram.uniforms.set("spotLight.linear", 0.09f)
        cubeShaderProgram.uniforms.set("spotLight.quadratic", 0.032f)
        cubeShaderProgram.uniforms.set("spotLight.cutOff", cos(12.5f.toRadians()))
        cubeShaderProgram.uniforms.set("spotLight.outerCutOff", cos(17.5f.toRadians()))

        gl.bindBuffer(ARRAY_BUFFER, cubeBuffer)
        cubeShaderProgram.enableAttributes()

        val axis: Vector = Vector(1.0f, 0.3f, 0.5f)
        for ((index, cubePosition) in cubePositions.withIndex()) {
            cubeShaderProgram.uniforms["model"] = Matrix { translate(cubePosition); rotate(axis, Angle(20.0f * index)) }
            gl.drawArrays(TRIANGLES, 0, 36)
        }

        // light
        gl.useProgram(lightShaderProgram)
        lightShaderProgram.uniforms["projection"] = projectionMatrix
        lightShaderProgram.uniforms["view"] = camera.viewMatrix()

        // >>
        /*
        gl.uniformMatrix4fv(gl.getUniformLocation(lightShader, "model"), false, lightModel)
        gl.bindBuffer(ARRAY_BUFFER, light)
        gl.bindAttributes(lightShaderSource)
        gl.drawArrays(TRIANGLES, 0, 36)
        */
        for (lightPosition in lightPositions) {
            lightShaderProgram.uniforms["model"] = Matrix { translate(lightPosition); scale(0.2f) }
            gl.bindBuffer(ARRAY_BUFFER, lightBuffer)
            lightShaderProgram.enableAttributes()
            gl.drawArrays(TRIANGLES, 0, 36)
        }
        // <<
    }

    input { event ->
        val x = event.x2
        val y = event.y2
        if (event.type == InputEventType.DRAG) {
            camera.processInput(x - mouse.x, mouse.y - y)
        }
        mouse.assign(x, y)
    }

    keyboard { event: KeyEvent ->
        if (event.type == KeyEventType.PRESS) {
            when (event.source) {
                KeyEventSource.W -> camera.processKeyboard(FORWARD, app.elapsedTime)
                KeyEventSource.S -> camera.processKeyboard(BACKWARD, app.elapsedTime)
                KeyEventSource.A -> camera.processKeyboard(LEFT, app.elapsedTime)
                KeyEventSource.D -> camera.processKeyboard(RIGHT, app.elapsedTime)
            }
        }
    }

}
