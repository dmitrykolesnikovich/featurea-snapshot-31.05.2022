package featurea

import kotlin.native.concurrent.ThreadLocal

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float, val specifier: String? = null) {
    constructor(value: Float) : this(value, value, value, value)
}

fun Color.toResource(): String {
    return "#${red.toHexString(255)}${green.toHexString(255)}${blue.toHexString(255)}${alpha.toHexString(255)}"
}

fun String.isValidColorResource(): Boolean {
    val rgba: String = replace("#", "")
    try {
        rgba.toLong(16)
        val success: Boolean = rgba.length == 8
        return success
    } catch (skip: Throwable) {
        return false
    }
}

fun String.toColor(): Color {
    return removePrefix("#").toLong(16).toColor()
}

fun Number.toColor(specifier: String? = null): Color {
    val rgba: Long = toLong()
    return ColorCache[rgba, specifier]
}

/*internals*/

@ThreadLocal
private object ColorCache {

    private val existingColors: MutableMap<Long, Color> = mutableMapOf()

    operator fun get(rgba: Long, specifier: String?): Color {
        // existing
        val existingColor: Color? = existingColors[rgba]
        if (existingColor != null) {
            return existingColor
        }

        // newly created
        val red: Float = (rgba shr 24 and 0xFF).toFloat() / 255f
        val green: Float = (rgba shr 16 and 0xFF).toFloat() / 255f
        val blue: Float = (rgba shr 8 and 0xFF).toFloat() / 255f
        val alpha: Float = (rgba and 0xFF).toFloat() / 255f
        @Suppress("NAME_SHADOWING")
        val specifier: String? = if (isInstrumentationEnabled) specifier else null
        val color: Color = Color(red, green, blue, alpha, specifier)
        existingColors[rgba] = color
        return color
    }

    fun clear() {
        existingColors.clear()
    }

}
