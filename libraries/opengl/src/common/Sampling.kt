package featurea.opengl

import featurea.utils.MutablePair
import featurea.utils.mto

class Sampling {
    var wrappingFunction: MutablePair<Int, Int> = REPEAT mto REPEAT
    var minificationFilter: Int = LINEAR
    var magnificationFilter: Int = LINEAR
}

fun textureWrapOf(value: String): Int = when (value) {
    "CLAMP_TO_EDGE" -> CLAMP_TO_EDGE
    "CLAMP_TO_BORDER" -> CLAMP_TO_BORDER
    "MIRRORED_REPEAT" -> MIRRORED_REPEAT
    "REPEAT" -> REPEAT
    "MIRROR_CLAMP_TO_EDGE" -> REPEAT
    else -> error("value: $value")
}
