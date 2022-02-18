package featurea

import java.io.File

data class BundleOptions(val projectFile: File, val bundleFile: File, val contentRoots: List<String>) {
    var command: String? = null
}
