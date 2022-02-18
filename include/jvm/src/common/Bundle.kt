package featurea

// Bundle is used by Content Library that's why I can not move it to Bundler Tool
// quickfix todo replace to Standard Library
class Bundle {
    val entries = mutableMapOf<String, ByteArray>()
    var excluded = mutableListOf("package", "texturePack") // todo add `excluded` property to bundle manifest file
    val manifest: Properties = Properties()
}

/*convenience*/

fun Bundle.toManifestPropertiesText(resources: Set<String>): String {
    val stringBuilder = StringBuilder()
    val packageId: String = manifest["package"] ?: error("package not defined")
    stringBuilder.append("package").append(PROPERTY_SEPARATOR).append(packageId).appendLine()
    stringBuilder.append("resources").append(PROPERTY_SEPARATOR).append(resources.joinToString()).appendLine()
    for ((key, value) in manifest) {
        if (excluded.contains(key)) {
            continue
        }
        val stringValue = when (value) {
            is List<*> -> value.joinToString()
            is Map<*, *> -> value.entries.joinToString()
            else -> value
        }
        stringBuilder.append(key).append(PROPERTY_SEPARATOR).append(stringValue).appendLine()
    }
    return stringBuilder.toString()
}

/*internals*/

private const val PROPERTY_SEPARATOR: String = "="
