@file:JvmName("Package")

package featurea.audio

import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    /*include(featurea.desktop.artifact)*/ // intentionally excluded
}