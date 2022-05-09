@file:Suppress("MoveVariableDeclarationIntoWhen")

package featurea.orientation

import android.content.res.Configuration
import featurea.android.FeatureaActivityListener
import featurea.layout.Orientation
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import featurea.window.Window

class OrientationChangeListener(override val module: Module) : Component, FeatureaActivityListener {

    private val window: Window = import() // IMPORTANT not lazy because reused over all app modules

    override suspend fun onConfigurationChanged(configuration: Configuration) {
        val orientation: Int = configuration.orientation
        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> window.updateOrientation(Orientation.LandscapeRight)
            Configuration.ORIENTATION_PORTRAIT -> window.updateOrientation(Orientation.Portrait)
            else -> error("orientation: $orientation")
        }
    }

}
