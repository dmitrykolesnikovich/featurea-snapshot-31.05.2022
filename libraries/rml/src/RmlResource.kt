@file:Suppress("RemoveExplicitTypeArguments", "UNCHECKED_CAST", "IfThenToElvis")

package featurea.rml

import featurea.content.ResourceTag
import featurea.content.appendChildResourceTag
import featurea.content.removeChildResourceTag
import featurea.rml.reader.RmlFile
import featurea.utils.Scope
import featurea.utils.Scope.Inner
import featurea.utils.Scope.Super

class RmlResource {

    lateinit var rmlTag: RmlTag
    lateinit var rmlFile: RmlFile
    var builder: RmlBuilder? = null

    internal val rmlTagByStartObject = linkedMapOf<Any, ResourceTag>() // todo make public if needed
    internal val startObjectByRmlTag = linkedMapOf<ResourceTag, Any>() // todo make public if needed
    val rmlTagByEndObject = linkedMapOf<Any, ResourceTag>() // IMPORTANT needed by editor delegate
    val endObjectByRmlTag = linkedMapOf<ResourceTag, Any>() // IMPORTANT needed by editor delegate

    suspend fun <T : Any> ResourceTag.createRmlTagEndObject(scope: Scope = Inner): T {
        val builder: RmlBuilder = checkNotNull(builder)
        val rmlResource: RmlResource = this@RmlResource
        val rmlTag: RmlTag = this

        // start object
        val link: RmlTag? = rmlTag.findLinkOrNull()
        val root: Any = if (link == null) {
            builder.create(rmlResource, rmlTag, scope)
        } else {
            link.createRmlTagEndObject(Super)
        }
        setRmlTagStartObject(rmlTag, scope, root)

        // attributes, properties, children
        for ((key, value) in rmlTag.attributes) rmlTag.assignAttribute(key, value, root, scope)
        for ((key, property) in rmlTag.properties) rmlTag.assignProperty(key, property, root, scope)
        for (child in rmlTag.children) rmlTag.assignChild(child, root, scope)

        // end object
        val rmlTagEndObject: Any = rmlTag.build(root, scope)
        setRmlTagEndObject(rmlTag, scope, rmlTagEndObject)
        return rmlTagEndObject as T
    }

    suspend fun ResourceTag.appendChild(child: ResourceTag) {
        appendChildResourceTag(child)

        // runtime
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val startObject: Any = startObject(rmlResource)
            val scope: Scope = Inner
            assignChild(child, startObject, scope)
        }
    }

    private suspend fun ResourceTag.assignChild(child: ResourceTag, startObject: Any, scope: Scope) {
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlTag: RmlTag = this
            val childEndObject: Any = child.createRmlTagEndObject<Any>(scope.nest())
            builder.append(rmlTag, scope, startObject, child, childEndObject)
        }
    }

    suspend fun ResourceTag.insertChild(index: Int, child: ResourceTag, startObject: Any, scope: Scope) {
        // data
        require(!children.contains(child))
        children.add(index, child)

        // >> todo make this consistent
        val childId: String? = child.idOrNull
        if (childId != null) {
            properties[childId] = child
        }
        // <<

        // runtime
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlTag: RmlTag = this
            val childEndObject: Any = child.createRmlTagEndObject<Any>(scope.nest())
            builder.insert(rmlTag, scope, startObject, index, child, childEndObject)
        }
    }

    suspend fun ResourceTag.removeChild(child: ResourceTag) {
        removeChildResourceTag(child)

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val rmlTag: RmlTag = this
            val startObject: Any = startObject(rmlResource)
            val endObject: Any = child.endObject(rmlResource)
            builder.remove(rmlTag, Inner, startObject, child, endObject)
            rmlTagByStartObject.remove(startObjectByRmlTag.remove(child))
            rmlTagByEndObject.remove(endObjectByRmlTag.remove(child))
        }
    }

    suspend fun ResourceTag.replaceChild(index: Int, child: ResourceTag) {
        // data
        require(children.contains(child))
        children.remove(child)
        if (index >= children.size) children.add(child) else children.add(index, child)

        // runtime
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val rmlTag: RmlTag = this
            val startObject: Any = startObject(rmlResource)
            val endObject: Any = child.endObject(rmlResource)
            builder.replace(rmlTag, Inner, startObject, child, index, endObject)
        }
    }

    suspend fun ResourceTag.assignAttribute(key: String, value: String) {
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val startObject: Any = startObject(rmlResource)
            val scope: Scope = Inner
            assignAttribute(key, value, startObject, scope)
        } else {
            // data
            attributes[key] = value
        }
    }

    suspend fun ResourceTag.assignAttribute(key: String, value: String, startObject: Any, scope: Scope) {
        // data
        attributes[key] = value

        // runtime
        val builder: RmlBuilder = checkNotNull(builder)
        val rmlTag: RmlTag = this
        builder.attributeOn(rmlTag, scope, startObject, key, value)
    }

    suspend fun ResourceTag.removeAttribute(key: String, value: String) {
        // data
        attributes.remove(key)

        // runtime
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val rmlTag: RmlTag = this
            val startObject: Any = startObject(rmlResource)
            builder.attributeOff(rmlTag, Inner, startObject, key, value)
        }
    }

    suspend fun ResourceTag.assignProperty(key: String, property: ResourceTag, startObject: Any, scope: Scope) {
        // data
        val id = property.attributes["id"]
        require(key == id) { "key: $key, id: $id" }
        require(!properties.contains(key))
        properties[key] = property

        // runtime
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlTag: RmlTag = this
            val propertyEndObject: Any = property.createRmlTagEndObject<Any>(scope.nest())
            builder.propertyOn(rmlTag, scope, startObject, key, property, propertyEndObject)
        }

    }

    suspend fun ResourceTag.removeProperty(key: String, property: ResourceTag) {
        // data
        val id = property.attributes["id"]
        require(key == id) { "key: $key, id: $id" }
        properties.remove(key)

        // runtime
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlTag: ResourceTag = this
            val rmlResource: RmlResource = this@RmlResource
            val startObject: Any = startObject(rmlResource)
            val endObject: Any = property.endObject(rmlResource)
            builder.propertyOff(rmlTag, Inner, startObject, key, property, endObject)
        }
    }

    suspend fun ResourceTag.buildAttribute(key: String, value: String) {
        assignAttribute(key, value)
        build()
    }

    suspend fun ResourceTag.build() {
        val builder: RmlBuilder? = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val startObject: Any = startObject(rmlResource)
            build(startObject, Inner)
        }
    }

    /*internals*/

    private suspend fun ResourceTag.build(startObject: Any, scope: Scope): Any {
        val rmlResource: RmlResource = this@RmlResource
        val rmlTag: RmlTag = this
        val builder: RmlBuilder = checkNotNull(builder)
        val original: Any = builder.build(rmlResource, rmlTag, scope, startObject)
        return original
    }

    private fun setRmlTagStartObject(rmlTag: ResourceTag, scope: Scope, rmlTagStartObject: Any) {
        if (scope == Inner) {
            check(rmlTagByStartObject.put(rmlTagStartObject, rmlTag) == null)
            check(startObjectByRmlTag.put(rmlTag, rmlTagStartObject) == null)
        }
    }

    private fun setRmlTagEndObject(rmlTag: ResourceTag, scope: Scope, rmlTagEndObject: Any) {
        if (scope == Inner) {
            check(rmlTagByEndObject.put(rmlTagEndObject, rmlTag) == null)
            check(endObjectByRmlTag.put(rmlTag, rmlTagEndObject) == null)
        }
    }

}

/*convenience*/

val RmlResource.canonicalClassName: String? get() = rmlFile.rmlSchema.canonicalClassNameByKey[rmlTag.name]
