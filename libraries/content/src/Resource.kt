package featurea.content

import featurea.Properties
import kotlin.reflect.KProperty

const val UNDEFINED_RESOURCE_PATH: String = "undefined"

class FileNotFoundException(path: String) : RuntimeException(path)

class ResourceNotFoundException(path: String) : RuntimeException(path)

class Resource(val manifest: Properties, val files: List<String>, val manifestFile: String?) {
    lateinit var canonicalName: String
        internal set
    lateinit var path: String
        internal set
}

fun Resource(files: List<String>, manifestFile: String? = null, props: () -> Properties = { Properties() }): Resource {
    return Resource(props(), files, manifestFile)
}

fun Resource(vararg files: String, manifestFile: String? = null, props: () -> Properties = { Properties() }): Resource {
    return Resource(props(), listOf(*files), manifestFile)
}

fun Resource.isDirectory(): Boolean {
    return !isFile()
}

fun Resource.isFile(): Boolean {
    return manifest["isFile"] ?: throw FileNotFoundException(path)
}

class ResourcePropertyDelegate<T : Any>(val key: String, val defaultValue: () -> T) {

    inline operator fun <reified T : Any> getValue(resource: Resource, property: KProperty<*>): T {
        return resource.manifest[key] ?: defaultValue() as T
    }

    inline operator fun <reified T : Any> setValue(resource: Resource, property: KProperty<*>, value: T) {
        resource.manifest[key] = value
    }

}
