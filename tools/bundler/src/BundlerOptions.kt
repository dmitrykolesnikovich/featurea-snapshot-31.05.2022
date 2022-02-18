package featurea.bundler

import featurea.BundleOptions
import java.io.File
import java.io.FileNotFoundException

fun Array<String>.toBundlerOptions(): BundleOptions {
    val rmlFilePath: String = this[0]
    val bundleFilePath: String = this[1]
    val contentRoots: List<String> = slice(2 until size)
    val rmlFile: File = File(rmlFilePath)
    val bundleFile: File = File(bundleFilePath)
    if (!rmlFile.exists()) throw FileNotFoundException(rmlFile.absolutePath)
    if (bundleFile.exists()) bundleFile.delete()
    return BundleOptions(rmlFile, bundleFile, contentRoots)
}
