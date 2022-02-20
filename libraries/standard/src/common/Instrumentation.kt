package featurea

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
var isPointOfInterestGained: Boolean = false

expect fun exitProcess(status: Int): Nothing

expect fun systemProperty(key: String): String?

fun breakpoint() {
    log("breakpoint")
}

object Tools {

    private val properties: LinkedHashMap<String, String> = linkedMapOf()

    operator fun set(name: String, file: String) {
        properties[name] = file
    }

    operator fun get(name: String): String? {
        val filePath = properties[name] ?: return null
        if (!existsFile(filePath)) return null
        return filePath
    }

}

fun enableInstrumentation(workingDir: String? = null) {
    isInstrumentationEnabled = true
    featurea.workingDir = workingDir
    configureTool("createFont")
    configureTool("extractGif")
    configureTool("packTextures")
}

private fun configureTool(toolName: String) {
    Tools[toolName] = "$featureaDir/engine/tools/$toolName/build/install/$toolName-shadow/bin/$toolName"
}
