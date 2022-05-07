package featurea.opengl

import featurea.utils.exitProcess
import featurea.utils.isInstrumentationEnabled
import featurea.utils.log
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.shader.reader.Attributes
import featurea.shader.reader.ShaderSource
import featurea.shader.reader.offsetOf
import featurea.shader.reader.withLineNumbers

abstract class Program(override val module: Module) : Component {

    private val gl: Opengl = import(OpenglProxy)

    val uniforms: Uniforms = Uniforms(this)
    val attributes: Attributes = Attributes()

    fun enableAttributes(type: Int = FLOAT) {
        val stride: Int = attributes.vertexSizeInBytes
        for (attribute in attributes) {
            gl.enableVertexAttribArray(attribute.location)
            gl.vertexAttributePointer(attribute.location, attribute.size, type, stride, attribute.offset)
        }
    }

    fun createBuffer(drawCallLimit: Int = 0, verticesPerDraw: Int, isMedium: Boolean = false): Buffer {
        val drawCallSize: Int = verticesPerDraw * attributes.vertexSize
        val buffer: Buffer = gl.createBuffer(drawCallSize, isMedium)
        buffer.ensureDrawCallLimit(drawCallLimit)
        return buffer
    }

    fun compile(shaderPath: String, source: ShaderSource) {
        /*
        log("Graphics.linkShaders")
        log(vertexSource)
        log("---")
        log(fragmentSource)
        log("---")
        */
        val program: Program = this
        val (vertexShaderSource, pixelShaderSource, attributes) = source

        val vertexShader: Shader = gl.createShader(VERTEX_SHADER)
        gl.shaderSource(vertexShader, vertexShaderSource)
        gl.compileShader(vertexShader)
        if (gl.getShaderParameter(vertexShader, COMPILE_STATUS) == FALSE) {
            log("${gl.getShaderInfoLog(vertexShader)}$shaderPath:vertex\n${source.vertexShaderSource.withLineNumbers()}")
            if (isInstrumentationEnabled) exitProcess(1)
        }

        val pixelShader: Shader = gl.createShader(FRAGMENT_SHADER)
        gl.shaderSource(pixelShader, pixelShaderSource)
        gl.compileShader(pixelShader)
        if (gl.getShaderParameter(pixelShader, COMPILE_STATUS) == FALSE) {
            log("${gl.getShaderInfoLog(pixelShader)}$shaderPath:pixel\n${source.pixelShaderSource.withLineNumbers()}")
            if (isInstrumentationEnabled) exitProcess(1)
        }

        gl.attachShader(program, vertexShader)
        gl.attachShader(program, pixelShader)
        gl.linkProgram(program)
        if (gl.getProgramParameter(program, LINK_STATUS) == FALSE) {
            log("${gl.getProgramInfoLog(program)}$shaderPath")
            if (isInstrumentationEnabled) exitProcess(1)
        }

        for ((index, attribute) in attributes.withIndex()) {
            val location: Int = gl.getAttributeLocation(program, attribute.name)
            check(location != -1)
            attribute.location = location
            attribute.offset = attributes.offsetOf(index)
        }
        program.attributes.init(attributes)

        gl.deleteShader(vertexShader)
        gl.deleteShader(pixelShader)
    }

    fun enable() {
        gl.useProgram(this)
        gl.program = this
    }

    fun disable() {
        gl.useProgram(null)
        gl.program = null
        bindBuffer(null) // quickfix todo improve
    }

    fun bindBuffer(buffer: Buffer?) {
        if (buffer != null) {
            gl.bindBuffer(ARRAY_BUFFER, buffer)
            val stride: Int = attributes.vertexSizeInBytes
            for (attribute in attributes) {
                gl.enableVertexAttribArray(attribute.location)
                gl.vertexAttributePointer(attribute.location, attribute.size, FLOAT, stride, attribute.offset)
            }
        } else {
            for (attribute in attributes) {
                gl.disableVertexAttributeArray(attribute.location)
            }
            gl.bindBuffer(ARRAY_BUFFER, null)
        }
    }

    fun bindTexture(name: String, slot: Int, texture: Texture?, sampling: Sampling = Sampling()) {
        if (texture != null) {
            // glsl: texture -> slot 0
            uniforms[name] = slot // texture -> slot 0
            // kotlin: texture -> slot 0
            gl.activeTexture(TEXTURE0 + slot) // -> slot 0
            gl.bindTexture(TEXTURE_2D, texture) // texture ->

            gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_S, sampling.wrappingFunction.first)
            gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_T, sampling.wrappingFunction.second)
            gl.textureParameter(TEXTURE_2D, TEXTURE_MIN_FILTER, sampling.minificationFilter)
            gl.textureParameter(TEXTURE_2D, TEXTURE_MAG_FILTER, sampling.magnificationFilter)
        } else {
            gl.bindTexture(TEXTURE_2D, null)
        }
    }

}
