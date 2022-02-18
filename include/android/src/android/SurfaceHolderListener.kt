package featurea.android

import android.view.SurfaceHolder

interface SurfaceHolderListener : SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
    override fun surfaceCreated(holder: SurfaceHolder) {}
}
