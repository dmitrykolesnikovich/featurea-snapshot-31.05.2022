@file:Suppress("RemoveExplicitTypeArguments")

package featurea.content

import featurea.utils.*

data class ResourceAttribute(val key: String, val value: String) {
    override fun equals(other: Any?): Boolean = if (other is ResourceAttribute) other.key == key else false
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "ResourceAttribute($key, $value)"
}

fun String.toStringAttributes(): Map<String, String> {
    return splitAndTrim(",").associate { it.toPair<String>(":") }
}

fun String.toFloatAttributes(): Map<String, Float> {
    return splitAndTrim(",").associate { it.toPair<Float>(":") }
}

fun String.toFloatAttributesList(): MutableList<MutablePair<String, Float>> {
    return toFloatAttributes().mapTo(mutableListOf()) { it.key mto it.value }
}

fun List<MutablePair<String, Float>>.toResource(): String {
    return joinToString { "${it.first}: ${it.second}" }
}
