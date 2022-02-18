package featurea.orientation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import featurea.android.MainActivityProxy
import featurea.layout.Orientation
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

@SuppressLint("SourceLockedOrientationActivity")
internal actual class OrientationServiceDelegate actual constructor(override val module: Module) : Component {

    private val mainActivity = import(MainActivityProxy)
    private val orientationService: OrientationService = import()

    actual var allowedOrientations: Collection<Orientation> = listOf()
        set(value) {
            field = value
            val isVertical = orientationService.isVerticalOrientationAllowed()
            val isHorizontal = orientationService.isHorizontalOrientationAllowed()
            if (isVertical && isHorizontal) {
                mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else if (isVertical) {
                mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } else if (isHorizontal) {
                mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }

    // IMPORTANT for better solution see https://stackoverflow.com/a/10383164/909169
    actual val currentOrientation: Orientation
        get() = when (mainActivity.resources.configuration.orientation) {
            ORIENTATION_LANDSCAPE -> Orientation.LandscapeRight
            ORIENTATION_PORTRAIT -> Orientation.Portrait
            else -> error("orientation: ${mainActivity.resources.configuration.orientation}")
        }

}
