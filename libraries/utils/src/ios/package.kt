package featurea.utils

import featurea.runtime.DependencyBuilder
import platform.Foundation.NSBundle
import featurea.System

actual fun DependencyBuilder.includeExternals() {
    static {
        val system: System = import()
        system.contentRoots.add(NSBundle.mainBundle.bundlePath)
    }
}