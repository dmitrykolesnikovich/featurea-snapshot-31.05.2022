@file:JvmName("Package")

package featurea.utils

import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.android.getApkFile
import featurea.jvm.normalizedPath
import featurea.runtime.DependencyBuilder
import featurea.System

actual fun DependencyBuilder.includeExternals() {
    static {
        val mainActivity: FeatureaActivity = import(MainActivityProxy)
        val system: System = import()
        system.contentRoots.add(mainActivity.getApkFile().normalizedPath)
    }
}
