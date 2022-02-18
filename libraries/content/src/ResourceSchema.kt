package featurea.content

import featurea.divide
import featurea.isClassPrimitive
import featurea.toSimpleName
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.get
import kotlin.collections.set

private const val INTERNAL = "internal"

class ResourceSchema {

    private val properties = mutableMapOf<String, String>()
    val canonicalClassNameByKey = linkedMapOf<String, String>()
    val simpleClassNameByKey = linkedMapOf<String, String>()
    val superCanonicalClassNameByKey = linkedMapOf<String, String?>()
    val rmlTagNameByCanonicalClassName = linkedMapOf<String, String>()
    private val superRmlTagNamesByRmlTagName = linkedMapOf<String, List<String>>()
    val attributeNamesByRmlTagName = ResourceAttributeNamesByRmlTagName()
    private val superRmlTagNameByRmlTagName = SuperRmlTagNameByRmlTagName()
    val attributesByTagName = linkedMapOf<String, MutableList<ResourceAttribute>>()

    operator fun get(key: String?): String? {
        return properties[key]
    }

    fun findSuperKeyForKeyOrNull(key: String): String? {
        val (rmlTagName, attributeName) = key.divide(".")
        val superRmlTagName: String = superRmlTagNameByRmlTagName[rmlTagName] ?: return null
        if (attributeName == null) {
            return superRmlTagName
        } else {
            return "$superRmlTagName.$attributeName"
        }
    }

    fun appendProperties(properties: Map<String, String>) {
        var currentRmlTagName: String? = null
        for ((key, value) in properties) {
            val (rmlTagName, attributeName) = key.divide(".")
            if (attributeName == null) {
                currentRmlTagName = rmlTagName
                createRmlTagNameProperty(rmlTagName, value)
            } else {
                requireNotNull(currentRmlTagName) { "$key: $value" }
                require(rmlTagName == currentRmlTagName) { "$key: $value" }
                createResourceAttributeNameProperty(rmlTagName, attributeName, value)
            }
        }
    }

    fun clearCaches() {
        properties.clear()
        canonicalClassNameByKey.clear()
        simpleClassNameByKey.clear()
        superCanonicalClassNameByKey.clear()
        rmlTagNameByCanonicalClassName.clear()
        superRmlTagNamesByRmlTagName.clear()
        attributesByTagName.clear()
    }

    /*internals*/

    private fun createRmlTagNameProperty(rmlTagName: String, rmlTagType: String) {
        val (canonicalClassName, superCanonicalClassName) = rmlTagType.divide(":") { "kotlin.Any" }
        val simpleClassName: String = canonicalClassName.toSimpleName()
        if (superCanonicalClassName == null && !canonicalClassName.isClassPrimitive()) {
            error("rmlTagType: $rmlTagType")
        }
        val key: String = rmlTagName
        properties[key] = rmlTagType
        canonicalClassNameByKey[key] = canonicalClassName
        simpleClassNameByKey[key] = simpleClassName
        superCanonicalClassNameByKey[key] = superCanonicalClassName
        rmlTagNameByCanonicalClassName[canonicalClassName] = rmlTagName
        superRmlTagNamesByRmlTagName[rmlTagName] = run {
            val result = mutableListOf<String>()
            var superRmlTagName: String? = rmlTagNameByCanonicalClassName[superCanonicalClassName]
            while (superRmlTagName != null) {
                result.add(superRmlTagName)
                superRmlTagName = superRmlTagNameByRmlTagName[superRmlTagName]
            }
            result
        }
        run/*create RML attribute names for superRmlTags from last to first*/ {
            val superRmlTagNames: List<String>? = superRmlTagNamesByRmlTagName[rmlTagName]
            if (superRmlTagNames != null && superRmlTagNames.isNotEmpty()) {
                val superRmlTagName: String = superRmlTagNames.first()
                val superAttributes: List<ResourceAttribute>? = attributesByTagName[superRmlTagName]
                if (superAttributes != null) {
                    for (superAttribute in superAttributes) {
                        createResourceAttributeNameProperty(rmlTagName, superAttribute.key, superAttribute.value)
                    }
                }
            }
        }
    }

    private fun createResourceAttributeNameProperty(tagName: String, attributeName: String, attributeType: String) {
        val (canonicalClassName, superCanonicalClassName) = attributeType.divide(":") { "kotlin.Any" }
        if (superCanonicalClassName == null && !canonicalClassName.isClassPrimitive() && canonicalClassName != INTERNAL) {
            error("attributeType: $attributeType")
        }
        val simpleClassName: String = canonicalClassName.toSimpleName()
        val key: String = "$tagName.$attributeName"
        properties[key] = attributeType
        canonicalClassNameByKey[key] = canonicalClassName
        simpleClassNameByKey[key] = simpleClassName
        superCanonicalClassNameByKey[key] = superCanonicalClassName
        attributesByTagName[tagName] = (attributesByTagName[tagName] ?: mutableListOf()).apply {
            val attribute: ResourceAttribute = ResourceAttribute(attributeName, attributeType)
            if (canonicalClassName != INTERNAL) {
                add(attribute)
            } else {
                remove(attribute)
            }
        }
    }

    inner class ResourceAttributeNamesByRmlTagName : Iterable<MutableList<ResourceAttribute>> {
        operator fun get(key: String?) = attributesByTagName[key]?.map { it.key } ?: emptyList()
        override fun iterator() = attributesByTagName.map { it.value }.iterator()
    }

    private inner class SuperRmlTagNameByRmlTagName {
        operator fun get(key: String?): String? = superRmlTagNamesByRmlTagName[key]?.firstOrNull()
        override fun toString() = superRmlTagNamesByRmlTagName.toString()
    }

}

// child: BlendTest, parent: Test
fun ResourceSchema.isSuper(child: String, parent: String): Boolean {
    var current: String? = child
    while (true) {
        if (current == parent) return true
        if (current == null) return false
        current = superCanonicalClassNameByKey[current]?.toSimpleName()
    }
}
