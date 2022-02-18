package featurea.font

data class FontPadding(val left: Double, val top: Double, val right: Double, val bottom: Double)

fun String.toFontPadding(): FontPadding {
    val (leftToken, topToken, rightToken, bottomToken) = split(",")
    return FontPadding(leftToken.toDouble(), topToken.toDouble(), rightToken.toDouble(), bottomToken.toDouble())
}
