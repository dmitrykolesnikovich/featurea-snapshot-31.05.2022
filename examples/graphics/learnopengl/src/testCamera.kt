// https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/1.getting_started/7.1.camera_circle/camera_circle.cpp
package featurea.examples.learnopengl

import featurea.input.InputEventType
import featurea.keyboard.KeyEventSource
import featurea.keyboard.KeyEventType
import featurea.math.Matrix
import featurea.math.Point
import featurea.math.Vector
import featurea.mto
import featurea.opengl.*
import featurea.shader.compile
import featurea.window.aspectRatio
import featurea.examples.learnopengl.Resources.smilePng
import featurea.examples.learnopengl.Resources.test3dShader

fun testCamera() = bootstrapTest {
    val projectionMatrix: Matrix = Matrix()
    val camera: Camera = Camera(pos = Vector(0f, 0f, 3f))
    val mouse: Point = Point(-1f, -1f)
    lateinit var buffer: Buffer
    lateinit var program: Program

    loader.loadResource(smilePng)

    init {
        diffuseTexturePath = smilePng
        sampling.wrappingFunction = CLAMP_TO_BORDER mto CLAMP_TO_BORDER
        near = 0.1f
        far = 100f
        fov = 45f
    }

    load {
        buffer = gl.createBuffer(0, 0)
        gl.bindBufferData(buffer, cubeVerticesWithUv, STATIC_DRAW)
        program = gl.createProgram()
        program.compile(test3dShader)
    }

    update {
        gl.clearColorBufferAndDepthBuffer(0.2f, 0.3f, 0.3f, 1.0f)
        gl.useProgram(program)
        gl.bindBuffer(ARRAY_BUFFER, buffer)
        program.enableAttributes()
        program.bindTexture("texture", 0, imageContent.findTexture(test.diffuseTexturePath))
        projectionMatrix.assignPerspective(test.near, test.far, camera.zoom, window.aspectRatio)
        program.uniforms["projection"] = projectionMatrix
        program.uniforms["view"] = camera.viewMatrix()
        for ((index, position) in cubePositions.withIndex()) {
            program.uniforms["module"] = Matrix { translate(position); rotate(1.0f, 0.3f, 0.5f, angle = 20.0 * index) }
            gl.drawArrays(TRIANGLES, 0, 36)
        }
    }

    input { event ->
        val x = event.x2
        val y = event.y2
        if (event.type == InputEventType.DRAG) {
            camera.processInput(x - mouse.x, mouse.y - y)
        }
        mouse.assign(x, y)
    }

    keyboard { event ->
        if (event.type == KeyEventType.PRESS) {
            when (event.source) {
                KeyEventSource.W -> camera.processKeyboard(CameraMovement.FORWARD, app.elapsedTime)
                KeyEventSource.S -> camera.processKeyboard(CameraMovement.BACKWARD, app.elapsedTime)
                KeyEventSource.A -> camera.processKeyboard(CameraMovement.LEFT, app.elapsedTime)
                KeyEventSource.D -> camera.processKeyboard(CameraMovement.RIGHT, app.elapsedTime)
            }
        }
    }

}
