package featurea.shader

import featurea.content.ContentType
import featurea.content.Resource
import featurea.opengl.Opengl
import featurea.opengl.OpenglProxy
import featurea.opengl.Program
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.shader.reader.ShaderSource

class ShaderContentType(override val module: Module) : Component, ContentType {

    private val gl: Opengl = import(OpenglProxy)
    private val shaderContent: ShaderContent = import()

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        if (shaderContent[resource.path] == null) {
            val program: Program = gl.createProgram()
            program.compile(resource.path)
            shaderContent[resource.path] = program
        }
        loadingQueue.add(resource.path)
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        // todo
        releaseQueue.add(resource.path)
    }

}

suspend fun Program.compile(shaderPath: String) {
    val shaderContent: ShaderContent = module.import()
    val source: ShaderSource = shaderContent.readShaderSource(shaderPath)
    compile(shaderPath, source)
}
