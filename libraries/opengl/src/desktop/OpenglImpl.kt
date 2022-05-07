package featurea.opengl

import com.jogamp.opengl.GL2
import featurea.desktop.jogamp.*
import featurea.jvm.BufferFactory.createByteBuffer
import featurea.jvm.BufferFactory.createFloatBuffer
import featurea.jvm.BufferFactory.createIntBuffer
import featurea.math.Matrix
import featurea.runtime.Module
import featurea.utils.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class BufferImpl(drawCallSize: Int, isMedium: Boolean, val instanceId: Int) : Buffer(drawCallSize, isMedium) {
    override fun toString(): String = "Buffer(instanceId=$instanceId)"
}

class ProgramImpl(module: Module, val instanceId: Int) : Program(module) {
    override fun toString(): String = "Program(instanceId=$instanceId)"
}

actual class Shader(val instanceId: Int) {
    override fun toString(): String = "Shader(instanceId=$instanceId)"
}

actual class Texture(val instanceId: Int) {
    override fun toString(): String = "Texture(instanceId=$instanceId)"
}

actual class UniformLocation(val instanceId: Int) {
    override fun toString(): String = "UniformLocation(instanceId=$instanceId)"
}

class OpenglImpl(module: Module) : Opengl(module) {

    lateinit var context: GL2

    private val floatBuffer16: FloatBuffer = createFloatBuffer(size = 16)
    private val floatBuffer1M: FloatBuffer = createFloatBuffer(size = 8_000_000)
    private val intBuffer1: IntBuffer = createIntBuffer(size = 1)
    private val intBuffer1M: IntBuffer = createIntBuffer(size = 1_000_000)
    private val logBuffer: ByteBuffer = createByteBuffer(size = 8192)
    private val logBuilder: StringBuilder = StringBuilder()

    override fun activeTexture(texture: Int) {
        checkAwtThread("activeTexture: $texture")
        context.glActiveTexture(texture)
    }

    override fun createProgram(): Program {
        checkAwtThread("createProgram")
        return ProgramImpl(module, checkNotZero(context.glCreateProgram()))
    }

    override fun bindAttributeLocation(program: Program, index: Int, name: String) {
        checkAwtThread("bindAttribLocation")
        program as ProgramImpl
        context.glBindAttribLocation(program.instanceId, index, name)
    }

    override fun attachShader(program: Program, shader: Shader) {
        checkAwtThread("attachShader")
        program as ProgramImpl
        context.glAttachShader(program.instanceId, shader.instanceId)
    }

    override fun linkProgram(program: Program) {
        checkAwtThread("linkProgram")
        program as ProgramImpl
        context.glLinkProgram(program.instanceId)
    }

    override fun getProgramParameter(program: Program, parameter: Int): Int {
        checkAwtThread("getProgramiv")
        program as ProgramImpl
        return context.getProgramiv(intBuffer1, program.instanceId, parameter)
    }

    override fun getShaderParameter(shader: Shader, parameter: Int): Int {
        checkAwtThread("getShaderiv")
        return context.getShaderiv(intBuffer1, shader.instanceId, parameter)
    }

    override fun getString(name: Int): String {
        checkAwtThread("getString")
        return context.glGetString(name)
    }

    override fun createShader(type: Int): Shader {
        checkAwtThread("createShader")
        return Shader(checkNotZero(context.glCreateShader(type)))
    }

    override fun deleteShader(shader: Shader) {
        checkAwtThread("deleteShader")
        context.glDeleteShader(shader.instanceId)
    }

    override fun shaderSource(shader: Shader, source: String) {
        checkAwtThread("shaderSource")
        context.glShaderSource(shader.instanceId, 1, arrayOf(source), null)
    }

    override fun compileShader(shader: Shader) {
        checkAwtThread("compileShader")
        context.glCompileShader(shader.instanceId)
    }

    override fun getProgramInfoLog(program: Program): String {
        checkAwtThread("getProgramInfoLog")
        program as ProgramImpl
        return context.getProgramInfoLog(logBuilder, logBuffer, intBuffer1, program.instanceId)
    }

    override fun getShaderInfoLog(shader: Shader): String {
        checkAwtThread("getShaderInfoLog")
        return context.getShaderInfoLog(logBuilder, logBuffer, intBuffer1, shader.instanceId)
    }

    override fun useProgram(program: Program?) {
        checkAwtThread("useProgram: $program")
        if (program != null) {
            program as ProgramImpl
            context.glUseProgram(program.instanceId)
        } else {
            context.glUseProgram(0)
        }
    }

    override fun enableVertexAttribArray(index: Int) {
        checkAwtThread("enableVertexAttribArray: $index")
        context.glEnableVertexAttribArray(index)
    }

    override fun vertexAttributePointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        checkAwtThread("vertexAttribPointer")
        context.glVertexAttribPointer(index, size, type, false, stride, offset.toLong())
    }

    override fun disableVertexAttributeArray(index: Int) {
        checkAwtThread("disableVertexAttribArray")
        context.glDisableVertexAttribArray(index)
    }

    override fun uniform(location: UniformLocation, matrix: Matrix) {
        checkAwtThread("uniformMatrix4fv: $location, $matrix")
        context.glUniformMatrix4fv(location.instanceId, 1, false, floatBuffer16.assign(matrix))
    }

    override fun uniform(location: UniformLocation, float: Float) {
        checkAwtThread("uniform1f: $location, $float")
        context.glUniform1f(location.instanceId, float)
    }

    override fun uniform(location: UniformLocation, int: Int) {
        checkAwtThread("uniform1i: $location, $int")
        context.glUniform1i(location.instanceId, int)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float) {
        checkAwtThread("uniform2f: $location, $float1, $float2")
        context.glUniform2f(location.instanceId, float1, float2)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float) {
        checkAwtThread("uniform3f")
        context.glUniform3f(location.instanceId, float1, float2, float3)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float, float4: Float) {
        checkAwtThread("uniform4f")
        context.glUniform4f(location.instanceId, float1, float2, float3, float4)
    }

    override fun getUniformLocation(program: Program, name: String): UniformLocation {
        checkAwtThread("getUniformLocation")
        program as ProgramImpl
        return UniformLocation(context.glGetUniformLocation(program.instanceId, name))
    }

    override fun getAttributeLocation(program: Program, name: String): Int {
        checkAwtThread("glGetAttribLocation")
        program as ProgramImpl
        return context.glGetAttribLocation(program.instanceId, name)
    }

    override fun drawArrays(mode: Int, first: Int, count: Int) {
        checkAwtThread("drawArrays: $mode, $first, $count")
        context.glDrawArrays(mode, first, count)
    }

    override fun drawElements(mode: Int, count: Int, type: Int, indices: IntArray) {
        checkAwtThread("drawElements")
        val indices: IntBuffer = intBuffer1M.apply { clear(); position(0); put(indices, 0, indices.size); rewind() }
        context.glDrawElements(mode, count, type, indices)
    }

    override fun createTexture(texturePath: String): Texture {
        checkAwtThread("createTexture")
        return Texture(checkNotZero(context.createTexture(intBuffer1)))
    }

    override fun enable(capability: Int) {
        checkAwtThread("enable")
        context.glEnable(capability)
        if (isPointOfInterestGained) {
            if (capability == SCISSOR_TEST) {
                log("enable: SCISSOR_TEST", allowDuplicates = false)
            }
        }
    }

    override fun cullFace(mode: Int) {
        checkAwtThread("cullFace")
        context.glCullFace(mode)
    }

    override fun blendFunction(sourceFactor: Int, destinationFactor: Int) {
        checkAwtThread("blendFunc")
        context.glBlendFunc(sourceFactor, destinationFactor)
    }

    override fun blendFunctionSeparate(srcRgb: Int, dstRgb: Int, srcAlpha: Int, dstAlpha: Int) {
        checkAwtThread("blendFuncSeparate")
        context.glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha)
    }

    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        checkAwtThread("blendEquationSeparate")
        context.glBlendEquationSeparate(modeRGB, modeAlpha)
    }

    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        checkAwtThread("blendColor")
        context.glBlendColor(red, green, blue, alpha)
    }

    override fun blendEquation(mode: Int) {
        checkAwtThread("blendEquation")
        context.glBlendEquation(mode)
    }

    override fun bindTexture(target: Int, texture: Texture?) {
        checkAwtThread("bindTexture: $target, $texture")
        if (texture != null) {
            context.glBindTexture(target, texture.instanceId)
        } else {
            context.glBindTexture(target, 0)
        }
    }

    override fun textureParameter(target: Int, parameter: Int, value: Int) {
        checkAwtThread("texParameteri: $target, $parameter, $value")
        context.glTexParameteri(target, parameter, value)
    }

    override fun generateMipmap(target: Int) {
        checkAwtThread("generateMipmap")
        context.glGenerateMipmap(target)
    }

    override fun disable(capability: Int) {
        checkAwtThread("disable")
        context.glDisable(capability)
        if (isPointOfInterestGained) {
            if (capability == SCISSOR_TEST) {
                log("disable: SCISSOR_TEST", allowDuplicates = false)
            }
        }
    }

    override fun deleteTexture(texture: Texture) {
        checkAwtThread("deleteTexture")
        context.glDeleteTextures(1, intBuffer1.apply { clear(); position(0); put(texture.instanceId); rewind() })
    }

    override fun bindBuffer(target: Int, buffer: Buffer?) {
        checkAwtThread("bindBuffer: $target, $buffer")
        if (buffer != null) {
            buffer as BufferImpl
            context.glBindBuffer(target, buffer.instanceId)
        } else {
            context.glBindBuffer(target, 0)
        }
    }

    override fun bufferData(target: Int, data: FloatArray, usage: Int) {
        checkAwtThread("bufferData")
        context.bufferData(floatBuffer1M, target, data, usage)
    }

    override fun bufferData(target: Int, data: IntArray, usage: Int) {
        checkAwtThread("bufferData")
        context.bufferData(intBuffer1M, target, data, usage)
    }

    override fun bufferSubData(target: Int, offset: Long, size: Long, data: FloatArray) {
        floatBuffer1M.apply { clear(); position(0); put(data, 0, data.size); rewind() }
        context.glBufferSubData(target, offset, data.floatArraySizeInBytes().toLong(), floatBuffer1M)
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        checkAwtThread("viewport")
        context.glViewport(x, y, width, height)
    }

    override fun clear(mask: Int) {
        checkAwtThread("clear")
        context.glClear(mask)
    }

    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        checkAwtThread("clearColor")
        context.glClearColor(red, green, blue, alpha)
    }

    override fun depthFunction(function: Int) {
        checkAwtThread("depthFunc")
        context.glDepthFunc(function)
    }

    override fun scissor(x: Int, y: Int, width: Int, height: Int) {
        checkAwtThread("scissor")
        context.glScissor(x, y, width, height)
        if (isPointOfInterestGained) {
            log("scissor: $x, $y, $width, $height", allowDuplicates = false)
        }
    }

    override fun deleteBuffer(buffer: Buffer) {
        checkAwtThread("deleteBuffer")
        buffer as BufferImpl
        context.glDeleteBuffers(1, intBuffer1.apply { clear(); position(0); put(buffer.instanceId); rewind() })
    }

    override fun lineWidth(width: Float) {
        checkAwtThread("lineWidth")
        context.glLineWidth(width)
    }

    override fun pixelStore(parameter: Int, value: Int) {
        checkAwtThread("pixelStorei")
        context.glPixelStorei(parameter, value)
    }

    override fun polygonMode(face: Int, mode: Int) {
        checkAwtThread("glPolygonMode")
        context.glPolygonMode(face, mode)
    }

    override fun createBuffer(drawCallSize: Int, isMedium: Boolean): Buffer {
        checkAwtThread("createBuffer")
        return BufferImpl(drawCallSize, isMedium, checkNotZero(context.genBuffer(intBuffer1)))
    }

}

/*internals*/

private fun checkAwtThread(openglCommand: String) {
    val currentThreadSpecifier: String = currentThreadSpecifier()
    if (currentThreadSpecifier != "Thread[AWT-EventQueue-0,6,main]") {
        breakpoint()
    }
}
