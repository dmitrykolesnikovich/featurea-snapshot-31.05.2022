package featurea.font.reader

const val FONT_TYPE_PREFIX: String = "font:/"

fun String.toFontProperties(): Map<String, Any> {
    val data: String = removePrefix(FONT_TYPE_PREFIX)
    val result = HashMap<String, Any>()
    val isBold: Boolean
    val isItalic: Boolean
    val nameAndSize: String = when {
        endsWith("_Bold_Italic") -> {
            isBold = true
            isItalic = true
            data.substring(0, data.length - "_Bold_Italic".length)
        }
        endsWith("_Bold_") -> {
            isBold = true
            isItalic = false
            data.substring(0, data.length - "_Bold_".length)
        }
        endsWith("_Italic") -> {
            isBold = false
            isItalic = true
            data.substring(0, data.length - "_Italic".length)
        }
        else -> {
            isBold = false
            isItalic = false
            data
        }
    }
    val (name, size) = nameAndSize.split("_")
    result["name"] = name
    result["size"] = size.toInt()
    result["bold"] = isBold
    result["italic"] = isItalic
    return result
}

fun Map<String, Any>.toFontContent(): String {
    val name: String = this["name"] as String
    val size: Int = this["size"] as Int
    val isBold: Boolean = this["bold"] as Boolean
    val isItalic: Boolean = this["italic"] as Boolean
    return "font:/${name}_${size}_${if (isBold) "Bold" else ""}_${if (isItalic) "Italic" else ""}"
}
