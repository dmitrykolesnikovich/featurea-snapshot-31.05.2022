package featurea.shader.reader

class ShaderAttribute(val name: String, val size: Int) {
    var location: Int = -1
    var offset: Int = -1
    override fun toString() = "Attribute(name=$name, size=$size, location=$location, offset=$offset)"
}

fun List<ShaderAttribute>.offsetOf(location: Int): Int {
    if (location == 0) return 0
    var result = 0
    for (index in 1..location) {
        result += this[index - 1].size * Float.SIZE_BYTES
    }
    return result
}

class Attributes : Iterable<ShaderAttribute> {

    private val data = mutableListOf<ShaderAttribute>()
    var vertexSize: Int = -1
        private set
    var vertexSizeInBytes: Int = -1
        private set

    operator fun get(index: Int): ShaderAttribute {
        return data[index]
    }

    override fun iterator(): Iterator<ShaderAttribute> {
        return data.iterator()
    }

    fun init(attributes: List<ShaderAttribute>) {
        data.clear()
        data.addAll(attributes)
        vertexSize = sumBy { it.size }
        vertexSizeInBytes = vertexSize * Float.SIZE_BYTES
    }

}
