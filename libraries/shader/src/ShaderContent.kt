package featurea.shader

import featurea.content.ResourceNotFoundException
import featurea.utils.exitProcess
import featurea.utils.isInstrumentationEnabled
import featurea.opengl.Program
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.shader.reader.ShaderSource
import featurea.shader.reader.transpileShaderSource
import featurea.text.TextContent

class ShaderContent(override val module: Module) : Component {

    private val textContent: TextContent = import()

    private val programs = mutableMapOf<String, Program>()

    fun findProgram(shaderPath: String): Program {
        val program: Program? = programs[shaderPath]
        if (program == null && isInstrumentationEnabled) {
            exitProcess(1)
        }
        return program ?: throw ResourceNotFoundException(shaderPath)
    }

    operator fun get(shaderPath: String): Program? {
        return programs[shaderPath]
    }

    operator fun set(shaderPath: String, shaderProgram: Program) {
        programs[shaderPath] = shaderProgram
    }

    suspend fun readShaderSource(shaderPath: String): ShaderSource {
        val text: String = textContent.findTextOrNull(shaderPath) ?: throw ResourceNotFoundException(shaderPath)
        val source: ShaderSource = transpileShaderSource(text)
        return source
    }

}
