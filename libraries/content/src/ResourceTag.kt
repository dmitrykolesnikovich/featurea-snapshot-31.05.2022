package featurea.content

class ResourceTag {

    val name: String
    var parent: ResourceTag?
        internal set
    var filePath: String
        internal set

    constructor(name: String) {
        this.name = name
        this.parent = null
        this.filePath = UNDEFINED_RESOURCE_PATH
    }

    constructor(name: String, parent: ResourceTag) {
        this.name = name
        this.parent = parent
        this.filePath = parent.filePath
    }

    constructor(name: String, filePath: String) {
        this.name = name
        this.parent = null
        this.filePath = filePath
    }

    val properties = mutableMapOf<String, ResourceTag>()
    val attributes = mutableMapOf<String, String>()
    val children = mutableListOf<ResourceTag>()
    val idOrNull: String? get() = attributes["id"]
    val root: ResourceTag get() = parent?.root ?: this
    val isRoot: Boolean get() = parent == null
    val packageId: String get() = root.attributes["package"] ?: throw error(filePath)

    override fun toString() = "ResourceTag(name: $name, properties: $properties, children: $children)"

}

fun ResourceTag.appendChildResourceTag(child: ResourceTag) {
    require(!children.contains(child))
    children.add(child)

    // >> todo make this consistent
    val childId = child.idOrNull
    if (childId != null) {
        properties[childId] = child
    }
    // <<

    // >> todo replace it to `assignChild` method by fixing RmlFileParser
    require(child.parent == null)
    child.parent = this
    child.filePath = this.filePath
    // <<
}

fun ResourceTag.removeChildResourceTag(child: ResourceTag) {
    require(children.contains(child))
    children.remove(child)
    require(child.parent != null)
    child.parent = null
    child.filePath = UNDEFINED_RESOURCE_PATH
}
