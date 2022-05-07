@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("NAME_SHADOWING")

package featurea.opengl

import featurea.ios.toCOpaque
import featurea.ios.toGLboolean
import featurea.math.Matrix
import featurea.math.copyToArray16
import featurea.runtime.Module
import featurea.utils.checkNotZero
import featurea.utils.floatArraySizeInBytes
import featurea.utils.intArraySizeInBytes
import kotlinx.cinterop.*
import platform.gles2.*
import platform.glescommon.GLcharVar
import platform.glescommon.GLintVar
import platform.glescommon.GLsizeiVar
import platform.glescommon.GLuintVar

class ProgramImpl constructor(module: Module, val instanceId: UInt) : Program(module)
actual class Shader constructor(val instanceId: UInt)
actual class Texture constructor(val instanceId: UInt)
actual class UniformLocation(val instanceId: Int)
class BufferImpl constructor(drawCallSize: Int, isMedium: Boolean, val instanceId: UInt) : Buffer(drawCallSize, isMedium)

// https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/ImplementingaMultitasking-awareOpenGLESApplication/ImplementingaMultitasking-awareOpenGLESApplication.html#//apple_ref/doc/uid/TP40008793-CH5-SW6
class OpenglImpl(module: Module) : Opengl(module) {

    private val floatArray16: FloatArray = FloatArray(size = 16)

    override fun activeTexture(texture: Int) {
        glActiveTexture(texture.toUInt())
    }

    override fun createProgram(): Program {
        return ProgramImpl(module, checkNotZero(glCreateProgram()))
    }

    override fun bindAttributeLocation(program: Program, index: Int, name: String) {
        program as ProgramImpl
        glBindAttribLocation(program.instanceId, index.toUInt(), name)
    }

    override fun attachShader(program: Program, shader: Shader) {
        program as ProgramImpl
        glAttachShader(program.instanceId, shader.instanceId)
    }

    override fun linkProgram(program: Program) {
        program as ProgramImpl
        glLinkProgram(program.instanceId)
    }

    override fun getProgramParameter(program: Program, parameter: Int): Int = memScoped {
        program as ProgramImpl
        val result = alloc<GLintVar>()
        glGetProgramiv(program.instanceId, parameter.toUInt(), result.ptr)
        return result.value
    }

    override fun getShaderParameter(shader: Shader, parameter: Int): Int = memScoped {
        val result = alloc<GLintVar>()
        glGetShaderiv(shader.instanceId, parameter.toUInt(), result.ptr)
        return result.value
    }

    override fun createShader(type: Int): Shader {
        return Shader(checkNotZero(glCreateShader(type.toUInt())))
    }

    override fun deleteShader(shader: Shader) {
        glDeleteShader(shader.instanceId)
    }

    override fun shaderSource(shader: Shader, source: String) = memScoped {
        glShaderSource(shader.instanceId, 1, cValuesOf(source.cstr.getPointer(memScope)), null)
    }

    override fun compileShader(shader: Shader) {
        glCompileShader(shader.instanceId)
    }

    override fun getProgramInfoLog(program: Program): String = memScoped {
        program as ProgramImpl
        val result = allocArray<GLcharVar>(8192)
        glGetProgramInfoLog(program.instanceId, 8192, alloc<GLsizeiVar>().ptr, result)
        return result.toKString()
    }

    override fun getShaderInfoLog(shader: Shader): String = memScoped {
        val result = allocArray<ByteVar>(8192)
        glGetShaderInfoLog(shader.instanceId, 8192, alloc<GLsizeiVar>().ptr, result)
        return result.toKString()
    }

    override fun useProgram(program: Program?) {
        if (program != null) {
            program as ProgramImpl
            glUseProgram(program.instanceId)
        } else {
            glUseProgram(0u)
        }
    }

    override fun enableVertexAttribArray(index: Int) {
        glEnableVertexAttribArray(index.toUInt())
    }

    override fun vertexAttributePointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        glVertexAttribPointer(index.toUInt(), size, type.toUInt(), false.toGLboolean(), stride, offset.toCOpaque())
    }

    override fun disableVertexAttributeArray(index: Int) {
        glDisableVertexAttribArray(index.toUInt())
    }

    override fun uniform(location: UniformLocation, matrix: Matrix) {
        glUniformMatrix4fv(location.instanceId, 1, 0, matrix.copyToArray16(floatArray16).refTo(0))
    }

    override fun uniform(location: UniformLocation, float: Float) {
        glUniform1f(location.instanceId, float)
    }

    override fun uniform(location: UniformLocation, int: Int) {
        glUniform1i(location.instanceId, int)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float) {
        glUniform2f(location.instanceId, float1, float2)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float) {
        glUniform3f(location.instanceId, float1, float2, float3)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float, float4: Float) {
        glUniform4f(location.instanceId, float1, float2, float3, float4)
    }

    override fun getUniformLocation(program: Program, name: String): UniformLocation {
        program as ProgramImpl
        val instanceId: Int = glGetUniformLocation(program.instanceId, name)
        return UniformLocation(instanceId)
    }

    override fun getAttributeLocation(program: Program, name: String): Int {
        program as ProgramImpl
        val attributeLocation: Int = glGetAttribLocation(program.instanceId, name)
        return attributeLocation
    }

    override fun drawArrays(mode: Int, first: Int, count: Int) {
        glDrawArrays(mode.toUInt(), first, count)
    }

    override fun drawElements(mode: Int, count: Int, type: Int, indices: IntArray) {
        glDrawElements(mode.toUInt(), count, type.toUInt(), indices.refTo(0))
    }

    override fun createTexture(texturePath: String): Texture = memScoped {
        val result = alloc<GLuintVar>()
        glGenTextures(1, result.ptr)
        val id: UInt = result.value
        if (id == 0u) {
            error("texturePath: $texturePath")
        }
        return Texture(id)
    }

    override fun enable(capability: Int) {
        glEnable(capability.toUInt())
    }

    override fun cullFace(mode: Int) {
        glCullFace(mode.toUInt())
    }

    override fun blendFunction(sourceFactor: Int, destinationFactor: Int) {
        glBlendFunc(sourceFactor.toUInt(), destinationFactor.toUInt())
    }

    override fun blendFunctionSeparate(srcRgb: Int, dstRgb: Int, srcAlpha: Int, dstAlpha: Int) {
        glBlendFuncSeparate(srcRgb.toUInt(), dstRgb.toUInt(), srcAlpha.toUInt(), dstAlpha.toUInt())
    }

    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        glBlendColor(red, green, blue, alpha)
    }

    override fun blendEquation(mode: Int) {
        glBlendEquation(mode.toUInt())
    }

    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        TODO("blendEquationSeparate")
    }

    override fun bindTexture(target: Int, texture: Texture?) {
        if (texture != null) {
            glBindTexture(target.toUInt(), texture.instanceId)
        } else {
            glBindTexture(target.toUInt(), 0u)
        }
    }

    override fun textureParameter(target: Int, parameter: Int, value: Int) {
        glTexParameteri(target.toUInt(), parameter.toUInt(), value)
    }

    override fun generateMipmap(target: Int) {
        glGenerateMipmap(target.toUInt())
    }

    override fun disable(capability: Int) {
        glDisable(capability.toUInt())
    }

    override fun deleteTexture(texture: Texture) = memScoped {
        glDeleteTextures(1, alloc<GLuintVar>().apply { value = texture.instanceId }.ptr)
    }

    override fun bindBuffer(target: Int, buffer: Buffer?) {
        if (buffer != null) {
            buffer as BufferImpl
            glBindBuffer(target.toUInt(), buffer.instanceId)
        } else {
            glBindBuffer(target.toUInt(), 0u)
        }
    }

    override fun bufferData(target: Int, data: FloatArray, usage: Int) {
        val target: UInt = target.toUInt()
        val size: Long = data.floatArraySizeInBytes().toLong()
        val usage: UInt = usage.toUInt()
        glBufferData(target = target, size = size, data = data.refTo(0), usage = usage)
    }

    override fun bufferData(target: Int, data: IntArray, usage: Int) {
        val target: UInt = target.toUInt()
        val size: Int = data.intArraySizeInBytes()
        val usage: UInt = usage.toUInt()
        glBufferData(target = target, size = size.toLong(), data = data.refTo(0), usage = usage)
    }

    override fun bufferSubData(target: Int, offset: Long, size: Long, data: FloatArray) {
        glBufferSubData(target.toUInt(), offset, size, data.refTo(0))
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        glViewport(x, y, width, height)
    }

    override fun clear(mask: Int) {
        glClear(mask.toUInt())
    }

    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        glClearColor(red, green, blue, alpha)
    }

    override fun depthFunction(function: Int) {
        glDepthFunc(function.toUInt())
    }

    override fun scissor(x: Int, y: Int, width: Int, height: Int) {
        glScissor(x, y, width, height) // todo make use of featurea.support.pixelsInPoint
    }

    override fun deleteBuffer(buffer: Buffer) = memScoped {
        buffer as BufferImpl
        glDeleteBuffers(1, alloc<GLuintVar>().apply { value = buffer.instanceId }.ptr)
    }

    override fun lineWidth(width: Float) {
        glLineWidth(width) // todo make use of featurea.support.pixelsInPoint
    }

    override fun pixelStore(parameter: Int, value: Int) {
        glPixelStorei(parameter.toUInt(), value)
    }

    override fun polygonMode(face: Int, mode: Int) {
        error("stub")
    }

    override fun createBuffer(drawCallSize: Int, isMedium: Boolean): Buffer = memScoped {
        val result = alloc<GLuintVar>()
        glGenBuffers(1, result.ptr)
        return BufferImpl(drawCallSize, isMedium, instanceId = checkNotZero(result.value))
    }

    override fun getString(name: Int): String {
        TODO("getString")
    }

}
