@file:JvmName("Package")

package featurea.opengl

import featurea.runtime.DependencyBuilder
import featurea.window.WindowPlugin

actual fun DependencyBuilder.includeExternals() {
    WindowPlugin {
        "provideOpenglProxy" to provideOpenglProxy
    }
}