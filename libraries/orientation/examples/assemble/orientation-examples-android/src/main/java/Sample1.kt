package featurea.orientation.samples.android

import android.os.Bundle
import android.view.View
import featurea.MainActivityProxy
import featurea.android.FeatureaActivity
import featurea.layout.LandscapeOrientations
import featurea.layout.PortraitOrientations
import featurea.orientation.OrientationService
import featurea.orientation.dependencies
import featurea.runtime.*
import featurea.runtime.Provide
import featurea.runtime.systemCall

@ProvideProxy(MainActivityProxy::class)
class Sample1 : FeatureaActivity() {

    private val orientationService by lazy { import<OrientationService>() }
    private val mainActivity: FeatureaActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        systemCall {
            Runtime {
                injectContainer(dependencies, "DefaultContainer")
                injectModule("DefaultModule")
                init { module ->
                    mainActivity.module = module
                    module.provideComponent(MainActivityProxy(mainActivity))
                }
            }
        }
    }

    fun setupVertical(view: View) {
        orientationService.allowedOrientations = PortraitOrientations
    }

    fun setupHorizontal(view: View) {
        orientationService.allowedOrientations = LandscapeOrientations
    }

}
