package featurea.opengl

import android.opengl.GLES20.*
import android.os.Build
import androidx.annotation.RequiresApi
import featurea.checkNotZero
import featurea.utils.floatArraySizeInBytes
import featurea.utils.intArraySizeInBytes
import featurea.jvm.*
import featurea.math.Matrix
import featurea.math.copyToArray16
import featurea.runtime.Module
import java.nio.FloatBuffer
import java.nio.IntBuffer

class BufferImpl(stride: Int, attributesPerDraw: Int, checkMediumPrecision: Boolean, val instance: Int) : Buffer(stride, attributesPerDraw, checkMediumPrecision)
class ProgramImpl(module: Module, val instance: Int) : Program(module)
actual class Shader(val instance: Int)
actual class Texture(val instance: Int)
actual class UniformLocation(val instance: Int)

@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
class OpenglImpl(module: Module) : Opengl(module) {

    private val floatArray16: FloatArray = FloatArray(size = 16)
    private val floatBuffer1M: FloatBuffer = BufferFactory.createFloatBuffer(size = 1_000_000) // quickfix todo improve
    private val intBuffer1: IntBuffer = BufferFactory.createIntBuffer(size = 1)
    private val intBuffer1M: IntBuffer = BufferFactory.createIntBuffer(size = 1_000_000) // quickfix todo improve

    override fun activeTexture(texture: Int) {
        glActiveTexture(texture)
    }

    override fun createProgram(): Program {
        return ProgramImpl(module, instance = checkNotZero(glCreateProgram()))
    }

    override fun bindAttributeLocation(program: Program, index: Int, name: String) {
        program as ProgramImpl
        glBindAttribLocation(program.instance, index, name)
    }

    override fun attachShader(program: Program, shader: Shader) {
        program as ProgramImpl
        glAttachShader(program.instance, shader.instance)
    }

    override fun linkProgram(program: Program) {
        program as ProgramImpl
        glLinkProgram(program.instance)
    }

    override fun getProgramParameter(program: Program, parameter: Int): Int {
        program as ProgramImpl
        return intBuffer1.firstInt { glGetProgramiv(program.instance, parameter, it) }
    }

    override fun getShaderParameter(shader: Shader, parameter: Int): Int {
        return intBuffer1.firstInt { glGetShaderiv(shader.instance, parameter, it) }
    }

    override fun getString(name: Int): String {
        return glGetString(name)
    }

    override fun createShader(type: Int): Shader {
        return Shader(instance = checkNotZero(glCreateShader(type)))
    }

    override fun deleteShader(shader: Shader) {
        glDeleteShader(shader.instance)
    }

    override fun shaderSource(shader: Shader, source: String) {
        glShaderSource(shader.instance, source)
    }

    override fun compileShader(shader: Shader) {
        glCompileShader(shader.instance)
    }

    override fun getProgramInfoLog(program: Program): String {
        program as ProgramImpl
        return glGetProgramInfoLog(program.instance)
    }

    override fun getShaderInfoLog(shader: Shader): String {
        return glGetShaderInfoLog(shader.instance)
    }

    override fun useProgram(program: Program?) {
        if (program != null) {
            program as ProgramImpl
            glUseProgram(program.instance)
        } else {
            glUseProgram(0)
        }
    }

    override fun enableVertexAttribArray(index: Int) {
        glEnableVertexAttribArray(index)
    }

    override fun vertexAttributePointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        glVertexAttribPointer(index, size, type, /*normalized*/false, stride, offset)
    }

    override fun disableVertexAttributeArray(index: Int) {
        glDisableVertexAttribArray(index)
    }

    override fun uniform(location: UniformLocation, matrix: Matrix) {
        glUniformMatrix4fv(location.instance, 1, false, matrix.copyToArray16(floatArray16), 0)
    }

    override fun uniform(location: UniformLocation, float: Float) {
        glUniform1f(location.instance, float)
    }

    override fun uniform(location: UniformLocation, int: Int) {
        glUniform1i(location.instance, int)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float) {
        glUniform2f(location.instance, float1, float2)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float) {
        glUniform3f(location.instance, float1, float2, float3)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float, float4: Float) {
        glUniform4f(location.instance, float1, float2, float3, float4)
    }

    override fun getUniformLocation(program: Program, name: String): UniformLocation {
        program as ProgramImpl
        return UniformLocation(instance = glGetUniformLocation(program.instance, name))
    }

    override fun getAttributeLocation(program: Program, name: String): Int {
        program as ProgramImpl
        return glGetAttribLocation(program.instance, name)
    }

    override fun drawArrays(mode: Int, first: Int, count: Int) {
        glDrawArrays(mode, first, count)
    }

    override fun drawElements(mode: Int, count: Int, type: Int, indices: IntArray) {
        glDrawElements(mode, count, type, intBuffer1M.rewindData(indices))
    }

    override fun createTexture(texturePath: String): Texture {
        return Texture(instance = checkNotZero(intBuffer1.firstInt { glGenTextures(1, it) }))
    }

    override fun enable(capability: Int) {
        glEnable(capability)
    }

    override fun cullFace(mode: Int) {
        glCullFace(mode)
    }

    override fun blendFunction(sourceFactor: Int, destinationFactor: Int) {
        glBlendFunc(sourceFactor, destinationFactor)
    }

    override fun blendFunctionSeparate(srcRgbFactor: Int, dstRgbFactor: Int, srcAlphaFactor: Int, dstAlphaFactor: Int) {
        glBlendFuncSeparate(srcRgbFactor, dstRgbFactor, srcAlphaFactor, dstAlphaFactor)
    }

    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        TODO("blendEquationSeparate")
    }

    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        glBlendColor(red, green, blue, alpha)
    }

    override fun blendEquation(mode: Int) {
        glBlendEquation(mode)
    }

    override fun bindTexture(target: Int, texture: Texture?) {
        if (texture != null) {
            glBindTexture(target, texture.instance)
        } else {
            glBindTexture(target, 0)
        }
    }

    override fun textureParameter(target: Int, parameter: Int, value: Int) {
        glTexParameteri(target, parameter, value)
    }

    override fun generateMipmap(target: Int) {
        glGenerateMipmap(target)
    }

    override fun disable(capability: Int) {
        glDisable(capability)
    }

    override fun deleteTexture(texture: Texture) {
        glDeleteTextures(1, intBuffer1.rewindFirst(texture.instance))
    }

    override fun bindBuffer(target: Int, buffer: Buffer?) {
        if (buffer != null) {
            buffer as BufferImpl
            glBindBuffer(target, buffer.instance)
        } else {
            glBindBuffer(target, 0)
        }
    }

    override fun bufferData(target: Int, data: FloatArray, usage: Int) {
        glBufferData(target, data.floatArraySizeInBytes(), floatBuffer1M.rewindData(data), usage)
    }

    override fun bufferData(target: Int, data: IntArray, usage: Int) {
        glBufferData(target, data.intArraySizeInBytes(), intBuffer1M.rewindData(data), usage)
    }

    override fun bufferSubData(target: Int, offset: Long, size: Long, data: FloatArray) {
        glBufferSubData(target, offset.toInt(), size.toInt(), floatBuffer1M.rewindData(data))
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        glViewport(x, y, width, height)
    }

    override fun clear(mask: Int) {
        glClear(mask)
    }

    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        glClearColor(red, green, blue, alpha)
    }

    override fun depthFunction(function: Int) {
        glDepthFunc(function)
    }

    override fun scissor(x: Int, y: Int, width: Int, height: Int) {
        glScissor(x, y, width, height)
    }

    override fun deleteBuffer(buffer: Buffer) {
        buffer as BufferImpl
        glDeleteBuffers(1, intBuffer1.rewindValue(buffer.instance))
    }

    override fun lineWidth(width: Float) {
        glLineWidth(width)
    }

    override fun pixelStore(parameter: Int, value: Int) {
        glPixelStorei(parameter, value)
    }

    override fun polygonMode(face: Int, mode: Int) = error("stub")

    override fun createBuffer(stride: Int, attributesPerDraw: Int, checkMediumPrecision: Boolean): Buffer {
        val instance: Int = checkNotZero(intBuffer1.firstInt { glGenBuffers(1, it) })
        return BufferImpl(stride, attributesPerDraw, checkMediumPrecision, instance)
    }

}
