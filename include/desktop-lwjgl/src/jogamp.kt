package featurea.desktop.jogamp

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLJPanel
import featurea.breakpoint
import featurea.floatArraySizeInBytes
import featurea.intArraySizeInBytes
import featurea.jvm.*
import featurea.math.Matrix
import javafx.application.Platform
import javafx.application.Preloader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun DefaultGLCapabilities(): GLCapabilities = GLCapabilities(GLProfile.getDefault())

fun MaxGLCapabilities(): GLCapabilities = GLCapabilities(GLProfile.getMaximum(true))

fun preloadOpenglNatives() {
    val panel: GLJPanel = GLJPanel(DefaultGLCapabilities())
    panel.destroy()
}

fun FloatBuffer.assign(value: Matrix): FloatBuffer = apply {
    clear()
    with(value) {
        put(m00); put(m01); put(m02); put(m03)
        put(m10); put(m11); put(m12); put(m13)
        put(m20); put(m21); put(m22); put(m23)
        put(m30); put(m31); put(m32); put(m33)
    }
    flip()
}

fun GL2.getProgramiv(result: IntBuffer, program: Int, pname: Int): Int =
    result.firstInt { glGetProgramiv(program, pname, result) }

fun GL2.getShaderiv(result: IntBuffer, program: Int, pname: Int): Int =
    result.firstInt { glGetShaderiv(program, pname, result) }

fun GL2.getProgramInfoLog(infoLog: StringBuilder, result: ByteBuffer, length: IntBuffer, program: Int): String {
    result.clear()
    length.clear()
    glGetProgramInfoLog(program, result.capacity(), length, result)
    return infoLog.replaceAll(result, length[0])
}

fun GL2.getShaderInfoLog(infoLog: StringBuilder, result: ByteBuffer, length: IntBuffer, program: Int): String {
    result.clear()
    length.clear()
    glGetShaderInfoLog(program, result.capacity(), length, result)
    return infoLog.replaceAll(result, length[0])
}

fun GL2.createTexture(result: IntBuffer): Int = result.firstInt { glGenTextures(1, result) }

fun GL2.genBuffer(result: IntBuffer): Int = result.firstInt { glGenBuffers(1, result) }

fun GL2.bufferData(dataBuffer: FloatBuffer, target: Int, data: FloatArray, usage: Int) {
    if (data.size > dataBuffer.capacity()) {
        breakpoint()
    }
    check(data.size <= dataBuffer.capacity())
    dataBuffer.apply { clear(); position(0); put(data, 0, data.size); rewind() }
    glBufferData(target, data.floatArraySizeInBytes().toLong(), dataBuffer, usage)
}

fun GL2.bufferData(dataBuffer: IntBuffer, target: Int, data: IntArray, usage: Int) {
    check(data.size <= dataBuffer.capacity())
    dataBuffer.apply { clear(); position(0); put(data, 0, data.size); rewind() }
    glBufferData(target, data.intArraySizeInBytes().toLong(), dataBuffer, usage)
}
