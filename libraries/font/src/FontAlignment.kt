package featurea.font

import featurea.Specified

enum class HAlign(override val specifier: String) : Specified {
    LEFT("left"),
    RIGHT("right"),
    CENTER("center")
}

enum class VAlign(override val specifier: String) : Specified {
    TOP("top"),
    BOTTOM("bottom"),
    MIDDLE("middle"),
    WRAP("wrap")
}
