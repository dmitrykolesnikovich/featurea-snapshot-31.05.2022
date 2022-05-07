package featurea.opengl

import featurea.utils.emptyString
import featurea.math.Matrix
import featurea.math.copyToArray16
import featurea.runtime.Module
import org.khronos.webgl.*

class BufferImpl(stride: Int, attributesPerDraw: Int, checkMediumPrecision: Boolean, val instance: WebGLBuffer) : Buffer(stride, attributesPerDraw, checkMediumPrecision)
class ProgramImpl(module: Module, val instance: WebGLProgram) : Program(module)
actual class Shader(val instance: WebGLShader)
actual class Texture(val instance: WebGLTexture)
actual class UniformLocation(val instance: WebGLUniformLocation?)

class OpenglImpl(module: Module) : Opengl(module) {

    private val floatArray16: FloatArray = FloatArray(size = 16)

    lateinit var context: WebGLRenderingContext

    override fun activeTexture(texture: Int) {
        context.activeTexture(texture)
    }

    override fun createProgram(): Program {
        return ProgramImpl(module, instance = checkNotNull(context.createProgram()))
    }

    override fun bindAttributeLocation(program: Program, index: Int, name: String) {
        program as ProgramImpl
        context.bindAttribLocation(program.instance, index, name)
    }

    override fun attachShader(program: Program, shader: Shader) {
        program as ProgramImpl
        context.attachShader(program.instance, shader.instance)
    }

    override fun linkProgram(program: Program) {
        program as ProgramImpl
        context.linkProgram(program.instance)
    }

    override fun getProgramParameter(program: Program, parameter: Int): Int {
        program as ProgramImpl
        return context.getProgramParameter(program.instance, parameter).toIntCode()
    }

    override fun getShaderParameter(shader: Shader, parameter: Int): Int {
        return context.getShaderParameter(shader.instance, parameter).toIntCode()
    }

    override fun getString(name: Int): String {
        return context.getParameter(name) as String
    }

    override fun shaderSource(shader: Shader, source: String) {
        context.shaderSource(shader.instance, source)
    }

    override fun compileShader(shader: Shader) {
        context.compileShader(shader.instance)
    }

    override fun getProgramInfoLog(program: Program): String {
        program as ProgramImpl
        return context.getProgramInfoLog(program.instance) ?: emptyString
    }

    override fun getShaderInfoLog(shader: Shader): String {
        return context.getShaderInfoLog(shader.instance) ?: emptyString
    }

    override fun useProgram(program: Program?) {
        program as ProgramImpl?
        context.useProgram(program?.instance)
    }

    override fun enableVertexAttribArray(index: Int) {
        context.enableVertexAttribArray(index)
    }

    override fun vertexAttributePointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) {
        context.vertexAttribPointer(index, size, type, false, stride, offset)
    }

    override fun disableVertexAttributeArray(index: Int) {
        context.disableVertexAttribArray(index)
    }

    override fun uniform(location: UniformLocation, matrix: Matrix) {
        context.uniformMatrix4fv(location.instance, false, matrix.copyToArray16(floatArray16) as Float32Array)
    }

    override fun uniform(location: UniformLocation, float: Float) {
        context.uniform1f(location.instance, float)
    }

    override fun uniform(location: UniformLocation, int: Int) {
        context.uniform1i(location.instance, int)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float) {
        context.uniform2f(location.instance, float1, float2)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float) {
        context.uniform3f(location.instance, float1, float2, float3)
    }

    override fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float, float4: Float) {
        context.uniform4f(location.instance, float1, float2, float3, float4)
    }

    override fun getUniformLocation(program: Program, name: String): UniformLocation {
        program as ProgramImpl
        return UniformLocation(instance = context.getUniformLocation(program.instance, name))
    }

    override fun getAttributeLocation(program: Program, name: String): Int {
        program as ProgramImpl
        val location: Int = context.getAttribLocation(program.instance, name)
        return location
    }

    override fun drawArrays(mode: Int, first: Int, count: Int) {
        context.drawArrays(mode, first, count)
    }

    override fun drawElements(mode: Int, count: Int, type: Int, indices: IntArray) {
        context.drawElements(mode, count, type, 0)
    }

    override fun createTexture(texturePath: String): Texture {
        return Texture(checkNotNull(context.createTexture()))
    }

    override fun enable(capability: Int) {
        if (hasCapability(capability)) {
            context.enable(capability)
        }
    }

    override fun cullFace(mode: Int) {
        context.cullFace(mode)
    }

    override fun blendFunction(sourceFactor: Int, destinationFactor: Int) {
        context.blendFunc(sourceFactor, destinationFactor)
    }

    override fun blendFunctionSeparate(
        srcRgbFactor: Int,
        dstRgbFactor: Int,
        srcAlphaFactor: Int,
        dstAlphaFactor: Int
    ) {
        context.blendFuncSeparate(srcRgbFactor, dstRgbFactor, srcAlphaFactor, dstAlphaFactor)

    }

    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        context.blendEquationSeparate(modeRGB, modeAlpha)
    }

    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        context.blendColor(red, green, blue, alpha)
    }

    override fun blendEquation(mode: Int) {
        context.blendEquation(mode)
    }

    override fun bindTexture(target: Int, texture: Texture?) {
        context.bindTexture(target, texture?.instance)
    }

    override fun textureParameter(target: Int, parameter: Int, value: Int) {
        context.texParameteri(target, parameter, value)
    }

    override fun generateMipmap(target: Int) {
        context.generateMipmap(target)
    }

    override fun disable(capability: Int) {
        if (hasCapability(capability)) {
            context.disable(capability)
        }
    }

    override fun deleteTexture(texture: Texture) {
        context.deleteTexture(texture.instance)
    }

    override fun deleteBuffer(buffer: Buffer) {
        buffer as BufferImpl
        context.deleteBuffer(buffer.instance)
    }

    override fun bindBuffer(target: Int, buffer: Buffer?) {
        buffer as BufferImpl?
        context.bindBuffer(target, buffer?.instance)
    }

    override fun bufferData(target: Int, data: FloatArray, usage: Int) {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "CAST_NEVER_SUCCEEDS")
        context.bufferData(target, data as BufferDataSource, usage)
    }

    override fun bufferData(target: Int, data: IntArray, usage: Int) {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "CAST_NEVER_SUCCEEDS")
        context.bufferData(target, data as BufferDataSource, usage)
    }

    override fun bufferSubData(target: Int, offset: Long, size: Long, data: FloatArray) {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "CAST_NEVER_SUCCEEDS")
        context.bufferSubData(target, offset.toInt(), data as BufferDataSource)
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        context.viewport(x, y, width, height)
    }

    override fun clear(mask: Int) {
        context.clear(mask)
    }

    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        context.clearColor(red, green, blue, alpha)
    }

    override fun depthFunction(function: Int) {
        context.depthFunc(function)
    }

    override fun createShader(type: Int): Shader {
        return Shader(checkNotNull(context.createShader(type)))
    }

    override fun deleteShader(shader: Shader) {
        context.deleteShader(shader.instance)
    }

    override fun scissor(x: Int, y: Int, width: Int, height: Int) {
        context.scissor(x, y, width, height)
    }

    override fun lineWidth(width: Float) {
        context.lineWidth(width)
    }

    override fun pixelStore(parameter: Int, value: Int) {
        context.pixelStorei(parameter, value)
    }

    override fun createBuffer(stride: Int, attributesPerDraw: Int, checkMediumPrecision: Boolean): Buffer {
        return BufferImpl(stride, attributesPerDraw, checkMediumPrecision, checkNotNull(context.createBuffer()))
    }

    override fun polygonMode(face: Int, mode: Int) = error("stub")

}

/*internals*/

private val skippedCapabilities: List<Int> = listOf(TEXTURE_2D)

private fun hasCapability(capability: Int): Boolean = !skippedCapabilities.contains(capability)

private fun Any?.toIntCode(): Int = when (this) {
    null -> 0
    is Boolean -> if (this) 1 else 0
    is Number -> toInt()
    else -> -1
}
