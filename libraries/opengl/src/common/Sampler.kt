package featurea.opengl

class Sampler(val slot: Int, var texture: Texture? = null, init: Sampling.() -> Unit = {}) {

    val sampling: Sampling = Sampling()

    init {
        sampling.init()
    }

    fun withTexture(texture: Texture?): Sampler {
        this.texture = texture
        return this
    }

}
