package featurea.keyboard

import android.annotation.TargetApi
import android.os.Build.VERSION_CODES
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.util.component1
import androidx.core.util.component2
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.android.contentView
import featurea.android.screenSizeDp
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

@TargetApi(VERSION_CODES.JELLY_BEAN)
class KeyboardEventProducer(override val module: Module) : Component, OnGlobalLayoutListener {

    private val keyboard: Keyboard = import()
    private val mainActivity: FeatureaActivity = import(MainActivityProxy)

    private var prevHeightDp: Int = -1

    override fun onCreateComponent() {
        mainActivity.contentView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onDeleteComponent() {
        mainActivity.contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val displayHeight = mainActivity.contentView.height
        val (surfaceWidth, surfaceHeight) = mainActivity.screenSizeDp
        val keyboardHeight = (displayHeight - surfaceHeight)
        if (keyboardHeight > 100) {
            if (prevHeightDp != -1 && prevHeightDp != surfaceHeight /*trash-holding*/) {
                keyboard.fireShowKeyboard(surfaceWidth, keyboardHeight)
            }
        } else if (keyboard.isVisible) {
            keyboard.fireHideKeyboard()
        }
        prevHeightDp = surfaceHeight
    }

}
