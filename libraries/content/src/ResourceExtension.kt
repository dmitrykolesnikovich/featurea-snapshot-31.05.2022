package featurea.content

import featurea.hasExtension
import featurea.isValidFilePath

const val pngExtension: String = "png"
const val mp3Extension: String = "mp3"
const val jpgExtension: String = "jpg"
const val jpegExtension: String = "jpeg"
const val gifExtension: String = "gif"
const val propertiesExtension: String = "properties"
const val shaderExtension: String = "shader"

fun String.isValidImageResource(): Boolean {
    return isValidFilePath() && hasExtension(pngExtension, jpgExtension, jpegExtension, gifExtension)
}

fun String.isValidAudioResource(): Boolean {
    return isValidFilePath() && hasExtension(mp3Extension)
}
