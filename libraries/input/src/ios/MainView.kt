package featurea.input

import featurea.ios.MainViewProxy
import featurea.ios.displayLinkWithTarget
import featurea.log
import featurea.runtime.ComponentProvider
import featurea.runtime.Module
import featurea.runtime.provide
import featurea.window.MainViewDelegate
import kotlinx.cinterop.CValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.EAGL.EAGLContext
import platform.EAGL.kEAGLRenderingAPIOpenGLES3
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSRunLoop
import platform.GLKit.GLKView
import platform.GLKit.GLKViewDrawableDepthFormat24
import platform.QuartzCore.CADisplayLink
import platform.UIKit.UIEvent

// IMPORTANT why `glDelegate` is not inlined: https://code.tutsplus.com/tutorials/what-is-exc_bad_access-and-how-to-debug-it--cms-24544
class MainView constructor(val module: Module, frame: CValue<CGRect>) : GLKView(frame) {

    private val mainView: MainView = this
    private val touchEventProducer: TouchEventProducer by lazy { module.importComponent() }

    val glDelegate = MainViewDelegate(module)
    val displayLink = CADisplayLink.displayLinkWithTarget {
        memScoped {
            mainView.display()
        }
    }

    // todo make use of
    fun destroy() {
        displayLink.removeFromRunLoop(NSRunLoop.currentRunLoop, NSDefaultRunLoopMode)
        EAGLContext.setCurrentContext(null)
    }

    fun onResize() {
        log("MainView.onResize")
    }

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        touchEventProducer.pushEvent(touches, InputEventType.DOWN)
    }

    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        touchEventProducer.pushEvent(touches, InputEventType.DRAG)
    }

    override fun touchesCancelled(touches: Set<*>, withEvent: UIEvent?) {
        touchEventProducer.pushEvent(touches, InputEventType.UP)
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        touchEventProducer.pushEvent(touches, InputEventType.UP)
    }

}

// todo make use of destroy
fun MainViewProvider() = ComponentProvider<MainViewProxy> { module: Module ->
    memScoped {
        log("MainViewProvider.create")
        val mainView = MainView(module, frame = CGRectZero.readValue())
        // mainView.addObserver("bounds") { mainView.onResize() } // quickfix todo uncomment
        val glContext = EAGLContext(kEAGLRenderingAPIOpenGLES3)
        mainView.setContext(glContext)
        mainView.setDrawableDepthFormat(GLKViewDrawableDepthFormat24)
        mainView.setDelegate(mainView.glDelegate)
        mainView.setEnableSetNeedsDisplay(false)
        EAGLContext.setCurrentContext(mainView.context)
        mainView.displayLink.addToRunLoop(NSRunLoop.currentRunLoop, NSDefaultRunLoopMode)
        provide(MainViewProxy(mainView))
    }
}
