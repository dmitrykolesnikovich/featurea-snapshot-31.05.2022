package featurea.rml

import featurea.content.ResourceTag
import featurea.utils.splitAndTrim

typealias RmlTag = ResourceTag

typealias ResourceAttributeBlock = suspend (resourceTag: ResourceTag, key: String, value: String) -> Unit

fun ResourceTag.startObject(rmlResource: RmlResource?): Any {
    return rmlResource?.startObjectByRmlTag?.get(this) ?: throw RuntimeException()
}

fun ResourceTag.endObject(rmlResource: RmlResource?): Any {
    return rmlResource?.endObjectByRmlTag?.get(this) ?: throw RuntimeException()
}

fun ResourceTag.indexOfSelf(): Int {
    return parent?.children?.indexOf(this) ?: -1
}

fun ResourceTag.findResource(): String {
    return if (isRoot) filePath else "${parent?.findResource()}:/${idOrNull}"
}

fun ResourceTag.isLeaf(): Boolean {
    return properties.isEmpty() && children.isEmpty()
}

fun ResourceTag.findAttribute(key: String): String? {
    val value: String? = attributes[key]
    if (value != null) return value
    val link: ResourceTag? = findLinkOrNull()
    if (link != null) return link.findAttribute(key)
    return null
}

fun ResourceTag.findLinkOrNull(): ResourceTag? {
    val link: String = attributes["link"] ?: return null
    val linkRmlTag: ResourceTag? = when {
        link.startsWith("/") -> root.properties[link]
        else -> parent!!.properties[link] ?: root.properties[link]
    }
    if (linkRmlTag == null) return null
    if (linkRmlTag.containsRecursively(this)) {
        error("recursion not supported")
    }
    return linkRmlTag
}

fun ResourceTag.deepCopy(parent: ResourceTag? = this.parent): ResourceTag {
    val copy: ResourceTag = if (parent != null) {
        ResourceTag(name, parent)
    } else {
        ResourceTag(name, filePath)
    }
    for ((key, value) in attributes) {
        copy.attributes[key] = value
    }
    for ((key, property) in properties) {
        copy.properties[key] = property
    }
    for (child in children) {
        copy.children.add(child.deepCopy(parent = copy))
    }
    return copy
}

fun ResourceTag.findPropertyByIdPath(idPath: String?): ResourceTag {
    return findPropertyByIdPathOrNull(idPath) ?: error("idPath: $idPath")
}

fun ResourceTag.findPropertyByIdPathOrNull(idPath: String?): ResourceTag? {
    if (idPath == null) return this
    var currentProperty: ResourceTag = this
    val ids: List<String> = idPath.splitAndTrim("/")
    for (id in ids) {
        currentProperty = currentProperty.properties[id] ?: return null
    }
    return currentProperty
}

fun ResourceTag.containsRecursively(child: ResourceTag): Boolean {
    var ancestor: ResourceTag? = child.parent
    while (true) {
        if (ancestor == null) return false
        if (ancestor == this) return true
        ancestor = ancestor.parent
    }
}

fun ResourceTag.findParent(predicate: (parent: ResourceTag) -> Boolean): ResourceTag? {
    var currentParent: ResourceTag? = parent
    while (currentParent != null) {
        if (predicate(currentParent)) return currentParent
        currentParent = currentParent.parent
    }
    return null
}

fun ResourceTag.anyParent(predicate: (parent: ResourceTag) -> Boolean): Boolean {
    return findParent(predicate) != null
}

suspend fun ResourceTag.forEachResourceAttribute(block: ResourceAttributeBlock) {
    for ((key, value) in attributes) {
        block(this, key, value)
    }
    for (child in children) {
        child.forEachResourceAttribute(block)
    }
}

fun ResourceTag.selectionPathRelativeTo(parentRmlTag: ResourceTag): IntArray {
    val list: ArrayList<Int> = ArrayList<Int>()
    var currentRmlTag: ResourceTag = this
    while (currentRmlTag.parent != null && currentRmlTag !== parentRmlTag) {
        list.add(currentRmlTag.parent!!.children.indexOf(currentRmlTag))
        currentRmlTag = currentRmlTag.parent!!
    }
    val listIndices: IntArray = IntArray(list.size)
    list.reverse()
    for (index in list.indices) {
        listIndices[index] = list[index]
    }
    return listIndices
}

fun ResourceTag.renameId(newId: String) {
    if (newId == idOrNull) return
    val resourceTag: ResourceTag = this

    val parentRmlTag: ResourceTag = parent!!
    val currentId: String? = idOrNull

    parentRmlTag.properties.remove(currentId)
    parentRmlTag.children.removeAll { it.idOrNull == currentId }

    attributes["id"] = newId

    parentRmlTag.properties[newId] = resourceTag
    parentRmlTag.children.add(resourceTag)
}
