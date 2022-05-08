@file:JvmName("Package")

package featurea.utils

import featurea.jvm.findZipFileOrNull
import featurea.jvm.normalizedPath
import featurea.jvm.userHomePath
import featurea.runtime.DependencyBuilder
import featurea.runtime.findContentRootsRecursively
import featurea.utils.isTargetDistPathExeFile
import java.io.File
import featurea.System

private val globalPaths: List<String> = listOf("", userHomePath)

actual fun DependencyBuilder.includeExternals() {
    static {
        val system: System = import()
        system.contentRoots.addAll(globalPaths)
        if (isTargetDistPathExeFile()) {
            val exeFile: File? = System::class.java.findZipFileOrNull()
            if (exeFile != null) {
                system.workingDir = exeFile.normalizedPath
                system.contentRoots.add(0, exeFile.normalizedPath)
            }
        } else {
            val contentRootsRecursively: List<String> = dependencyRegistry.artifact.findContentRootsRecursively()
            system.contentRoots.addAll(contentRootsRecursively)
        }
        // log("[Standard] contentRoots: ${system.contentRoots}")
    }
}
