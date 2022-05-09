package featurea.utils

import featurea.utils.Tools.registerTool

const val GIF_CACHE_PATH: String = ".featurea/cache/gifs"
const val FONT_CACHE_PATH: String = ".featurea/cache/fonts"
const val SHADER_CACHE_PATH: String = ".featurea/cache/shaders"

var isInstrumentationEnabled: Boolean = false
    private set

var workingDir: String? = null
    private set

expect val featureaDir: String // todo replace `featureaDir` with `workingDir`

var alwaysCheckMediumPrecision: Boolean = false
var isStandardShaderLibraryIncluded: Boolean = true
var isPointOfInterestGained: Boolean = false // just for debug todo delete this

expect fun exitProcess(status: Int): Nothing

expect fun systemProperty(key: String): String?

fun breakpoint() {
    log("breakpoint")
}

fun enableInstrumentation(workingDir: String? = null) {
    isInstrumentationEnabled = true
    featurea.utils.workingDir = workingDir
    // >> quickfix todo improve
    registerTool("createFont")
    registerTool("extractGif")
    registerTool("packTextures")
    // <<
}
