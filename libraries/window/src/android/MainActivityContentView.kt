package featurea.window

import android.annotation.SuppressLint
import android.os.Build
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.android.androidContext
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

@SuppressLint("ViewConstructor")
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class MainActivityContentView(override val module: Module) : Component, RelativeLayout(module.androidContext) {

    val mainActivity: FeatureaActivity by lazy { import(MainActivityProxy) }
    val window: Window by lazy { import() }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        mainActivity.runOnUiThread {
            window.updateLayout()
        }
    }

}
