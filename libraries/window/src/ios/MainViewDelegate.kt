package featurea.window

import featurea.runtime.Module
import kotlinx.cinterop.CValue
import kotlinx.coroutines.runBlocking
import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
import platform.CoreGraphics.CGRect
import platform.GLKit.GLKView
import platform.GLKit.GLKViewDelegateProtocol
import platform.darwin.NSObject

class MainViewDelegate(module: Module) : NSObject(), GLKViewDelegateProtocol {

    private val window: Window = module.importComponent()

    private var past: Double = -1.0

    override fun glkView(view: GLKView, drawInRect: CValue<CGRect>) {
        initRuntimeIfNeeded()
        val now: Double = CFAbsoluteTimeGetCurrent()
        if (past == -1.0) {
            past = now
        }
        val elapsedTime: Double = (now - past) * 1000.0
        try {
            runBlocking {
                // todo place resize logic here
                window.update(elapsedTime.toFloat())
            }
        } finally {
            past = now
        }
    }

}
