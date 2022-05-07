package featurea.rml

import featurea.content.ResourceTag
import featurea.content.appendChildResourceTag
import featurea.content.removeChildResourceTag
import featurea.utils.Scope.INNER
import featurea.utils.Scope.SUPER
import featurea.rml.reader.RmlFile
import featurea.utils.Scope

class RmlResourceNotFoundException(resource: String) : RuntimeException(resource)

class RmlResource {

    lateinit var rmlTag: ResourceTag
    lateinit var rmlFile: RmlFile
    var builder: RmlBuilder? = null

    internal val rmlTagByStartObject = linkedMapOf<Any, ResourceTag>() // todo make public if needed
    internal val startObjectByRmlTag = linkedMapOf<ResourceTag, Any>() // todo make public if needed
    val rmlTagByEndObject = linkedMapOf<Any, ResourceTag>() // IMPORTANT needed by editor delegate
    val endObjectByRmlTag = linkedMapOf<ResourceTag, Any>() // IMPORTANT needed by editor delegate

    suspend fun <T : Any> ResourceTag.createRmlTagEndObject(access: Scope = INNER): T {
        val builder: RmlBuilder = builder!!
        val rmlResource = this@RmlResource
        val rmlTag = this

        // start object
        val link = rmlTag.findLinkOrNull()
        val root = if (link == null) {
            builder.create(rmlResource, rmlTag, access)
        } else {
            link.createRmlTagEndObject(SUPER)
        }
        setRmlTagStartObject(rmlTag, access, root)

        // attributes, properties, children
        for ((key, value) in rmlTag.attributes) rmlTag.assignAttribute(key, value, root, access)
        for ((key, property) in rmlTag.properties) rmlTag.assignProperty(key, property, root, access)
        for (child in rmlTag.children) rmlTag.assignChild(child, root, access)

        // end object
        val rmlTagEndObject = rmlTag.build(root, access)
        setRmlTagEndObject(rmlTag, access, rmlTagEndObject)
        return rmlTagEndObject as T
    }

    suspend fun ResourceTag.appendChild(child: ResourceTag) {
        appendChildResourceTag(child)

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val startObject: Any = startObject(rmlResource)
            val access: Scope = INNER
            assignChild(child, startObject, access)
        }
    }

    private suspend fun ResourceTag.assignChild(child: ResourceTag, startObject: Any, access: Scope) {
        val builder = builder
        if (builder != null) {
            val rmlResource: RmlResource = this@RmlResource
            val rmlTag: ResourceTag = this
            val childEndObject: Any = child.createRmlTagEndObject<Any>(access.nest())
            builder.append(/*rmlResource, */rmlTag, access, startObject, child, childEndObject)
        }
    }

    suspend fun ResourceTag.insertChild(index: Int, child: ResourceTag, startObject: Any, access: Scope) {
        // data
        require(!children.contains(child))
        children.add(index, child)

        // >> todo make this consistent
        val childId = child.idOrNull
        if (childId != null) {
            properties[childId] = child
        }
        // <<

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val rmlTag = this
            val childEndObject = child.createRmlTagEndObject<Any>(access.nest())
            builder.insert(/*rmlResource, */rmlTag, access, startObject, index, child, childEndObject)
        }
    }

    suspend fun ResourceTag.removeChild(child: ResourceTag) {
        removeChildResourceTag(child)

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val rmlTag = this
            val startObject = startObject(rmlResource)
            val endObject = child.endObject(rmlResource)
            builder.remove(/*rmlResource, */rmlTag, INNER, startObject, child, endObject)
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
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val rmlTag = this
            val startObject = startObject(rmlResource)
            val endObject = child.endObject(rmlResource)
            builder.replace(/*rmlResource, */rmlTag, INNER, startObject, child, index, endObject)
        }
    }

    suspend fun ResourceTag.assignAttribute(key: String, value: String) {
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val startObject: Any = startObject(rmlResource)
            val access: Scope = INNER
            assignAttribute(key, value, startObject, access)
        } else {
            // data
            attributes[key] = value
        }
    }

    suspend fun ResourceTag.assignAttribute(key: String, value: String, startObject: Any, access: Scope) {
        // data
        attributes[key] = value

        // runtime
        val builder = builder!!
        val rmlResource = this@RmlResource
        val rmlTag = this
        builder.attributeOn(/*rmlResource, */rmlTag, access, startObject, key, value)
    }

    suspend fun ResourceTag.removeAttribute(key: String, value: String) {
        // data
        attributes.remove(key)

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val rmlTag = this
            val startObject = startObject(rmlResource)
            builder.attributeOff(/*rmlResource, */rmlTag, INNER, startObject, key, value)
        }
    }

    suspend fun ResourceTag.assignProperty(key: String, property: ResourceTag, startObject: Any, access: Scope) {
        // data
        val id = property.attributes["id"]
        require(key == id) { "key: $key, id: $id" }
        require(!properties.contains(key))
        properties[key] = property

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val rmlTag = this
            val propertyEndObject = property.createRmlTagEndObject<Any>(access.nest())
            builder.propertyOn(/*rmlResource, */rmlTag, access, startObject, key, property, propertyEndObject)
        }

    }

    suspend fun ResourceTag.removeProperty(key: String, property: ResourceTag) {
        // data
        val id = property.attributes["id"]
        require(key == id) { "key: $key, id: $id" }
        properties.remove(key)

        // runtime
        val builder = builder
        if (builder != null) {
            val rmlTag = this
            val rmlResource = this@RmlResource
            val startObject = startObject(rmlResource)
            val endObject = property.endObject(rmlResource)
            builder.propertyOff(/*rmlResource, */rmlTag, INNER, startObject, key, property, endObject)
        }
    }

    suspend fun ResourceTag.buildAttribute(key: String, value: String) {
        assignAttribute(key, value)
        build()
    }

    suspend fun ResourceTag.build() {
        val builder = builder
        if (builder != null) {
            val rmlResource = this@RmlResource
            val startObject = startObject(rmlResource)
            build(startObject, INNER)
        }
    }

    /*internals*/

    private suspend fun ResourceTag.build(startObject: Any, access: Scope): Any {
        val rmlResource = this@RmlResource
        val rmlTag = this
        val builder = builder!!
        val original = builder.build(rmlResource, rmlTag, access, startObject)
        return original
    }


    private fun setRmlTagStartObject(rmlTag: ResourceTag, access: Scope, rmlTagStartObject: Any) {
        if (access == INNER) {
            check(rmlTagByStartObject.put(rmlTagStartObject, rmlTag) == null)
            check(startObjectByRmlTag.put(rmlTag, rmlTagStartObject) == null)
        }
    }

    private fun setRmlTagEndObject(rmlTag: ResourceTag, access: Scope, rmlTagEndObject: Any) {
        if (access == INNER) {
            check(rmlTagByEndObject.put(rmlTagEndObject, rmlTag) == null)
            check(endObjectByRmlTag.put(rmlTag, rmlTagEndObject) == null)
        }
    }

}

val RmlResource.canonicalClassName: String? get() = rmlFile.rmlSchema.canonicalClassNameByKey[rmlTag.name]
