@file:JvmName("Package")

package featurea

import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.android.getApkFile
import featurea.jvm.normalizedPath
import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    static {
        val mainActivity: FeatureaActivity = import(MainActivityProxy)
        val system: System = import()

        system.contentRoots.add(mainActivity.getApkFile().normalizedPath)
    }
}
