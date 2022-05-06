package featurea.opengl

import featurea.utils.Color
import featurea.math.*
import featurea.runtime.import

// https://stackoverflow.com/a/16622177/909169
class Uniforms(val program: Program) {

    private val gl: Opengl = program.import(OpenglProxy)

    operator fun set(name: String, b1: Boolean) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, if (b1) 1 else 0)
    }

    operator fun set(name: String, i1: Int) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, i1)
    }

    operator fun set(name: String, f1: Float) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, f1)
    }

    operator fun set(name: String, f1: Float, f2: Float) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, f1, f2)
    }

    operator fun set(name: String, f1: Float, f2: Float, f3: Float) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, f1, f2, f3)
    }

    operator fun set(name: String, f1: Float, f2: Float, f3: Float, f4: Float) {
        val location: UniformLocation = findLocation(name)
        gl.uniform(location, f1, f2, f3, f4)
    }

    operator fun set(name: String, color: Color) {
        val location: UniformLocation = findLocation(name)
        gl.uniform(location, color.red, color.green, color.blue, color.alpha)
    }

    operator fun set(name: String, matrix: Matrix4) {
        val location: UniformLocation = findLocation(name)
        gl.uniform(location, matrix)
    }

    operator fun set(name: String, vector: Vector2) {
        val location: UniformLocation = findLocation(name)
        gl.uniform(location, vector.x, vector.y) // quickfix todo
    }

    operator fun set(name: String, vector: Vector) {
        val (x, y, z) = vector
        val location: UniformLocation = findLocation(name)
        gl.uniform(location, x, y, z)
    }

    operator fun set(name: String, size: Size) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, size.width, size.height)
    }

    operator fun set(name: String, rect: Rectangle) {
        val location: UniformLocation = findLocation(name)
        return gl.uniform(location, rect.x1, rect.y1, rect.x2, rect.y2)
    }

    operator fun set(name: String, sampler: Sampler) {
        val location: UniformLocation = findLocation(name)
        gl.uniform(location, sampler.slot)
        gl.activeTexture(TEXTURE0 + sampler.slot)
        gl.bindTexture(TEXTURE_2D, sampler.texture)
        val sampling: Sampling = sampler.sampling
        gl.textureParameter(TEXTURE_2D, TEXTURE_MIN_FILTER, sampling.minificationFilter)
        gl.textureParameter(TEXTURE_2D, TEXTURE_MAG_FILTER, sampling.magnificationFilter)
        gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_S, sampling.wrappingFunction.first)
        gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_T, sampling.wrappingFunction.second)
    }

    /*internals*/

    private val locations = mutableMapOf<String, UniformLocation>()

    private fun findLocation(name: String): UniformLocation {
        // existing
        val existingLocation: UniformLocation? = locations[name]
        if (existingLocation != null) {
            return existingLocation
        }

        // newly created
        val location: UniformLocation = gl.getUniformLocation(program, name)
        locations[name] = location
        return location
    }

}
