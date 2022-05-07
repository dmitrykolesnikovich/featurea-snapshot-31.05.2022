package featurea

import featurea.runtime.DependencyBuilder
import platform.Foundation.NSBundle

actual fun DependencyBuilder.includeExternals() {
    static {
        val system: System = import()
        system.contentRoots.add(NSBundle.mainBundle.bundlePath)
    }
}