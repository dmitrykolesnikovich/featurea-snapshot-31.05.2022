package featurea.android

import android.opengl.GLSurfaceView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import featurea.runtime.Artifact
import featurea.runtime.Delegate
import featurea.runtime.Proxy

/*dependencies*/

val artifact = Artifact("featurea.android") {
    "MainActivityContentViewProxy" to MainActivityContentViewProxy::class
    "MainActivityProxy" to MainActivityProxy::class
    "MainRenderProxy" to MainRenderProxy::class
    "MainSurfaceViewProxy" to MainSurfaceViewProxy::class
    "ProgressBarProxy" to ProgressBarProxy::class
    "ProgressLayoutProxy" to ProgressLayoutProxy::class
    "ProgressTextViewProxy" to ProgressTextViewProxy::class
    "RootLayoutProxy" to RootLayoutProxy::class
}

class MainActivityContentViewProxy(override val delegate: RelativeLayout) : Proxy<RelativeLayout> {
    companion object : Delegate<RelativeLayout>(MainActivityContentViewProxy::class)
}

class MainActivityProxy(override val delegate: FeatureaActivity) : Proxy<FeatureaActivity> {
    companion object : Delegate<FeatureaActivity>(MainActivityProxy::class)
}

class MainRenderProxy(override val delegate: GLSurfaceView.Renderer) : Proxy<GLSurfaceView.Renderer> {
    companion object : Delegate<GLSurfaceView.Renderer>(MainRenderProxy::class)
}

class MainSurfaceViewProxy(override val delegate: GLSurfaceView) : Proxy<GLSurfaceView> {
    companion object : Delegate<GLSurfaceView>(MainSurfaceViewProxy::class)
}

class ProgressBarProxy(override val delegate: ProgressBar) : Proxy<ProgressBar> {
    companion object : Delegate<ProgressBar>(ProgressBarProxy::class)
}

class ProgressLayoutProxy(override val delegate: ViewGroup) : Proxy<ViewGroup> {
    companion object : Delegate<ViewGroup>(ProgressLayoutProxy::class)
}

class ProgressTextViewProxy(override val delegate: TextView) : Proxy<TextView> {
    companion object : Delegate<TextView>(ProgressTextViewProxy::class)
}

class RootLayoutProxy(override val delegate: LinearLayout) : Proxy<LinearLayout> {
    companion object : Delegate<LinearLayout>(RootLayoutProxy::class)
}
