package featurea.input

import featurea.ios.MainViewProxy
import featurea.ios.addTarget
import featurea.ios.pixelsInPoint
import featurea.ios.value
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import platform.CoreGraphics.CGPoint
import platform.GLKit.GLKView
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.UITouch
import platform.UIKit.addGestureRecognizer

class TouchEventProducer constructor(override val module: Module) : Component {

    private val input: Input = import()
    private val mainView: GLKView = import(MainViewProxy)

    private val longPressGesture = UILongPressGestureRecognizer()

    override fun onCreateComponent() {
        longPressGesture.addTarget {
            val point = longPressGesture.locationInView(mainView).value
            pushEvent(point, InputEventType.LONG_TOUCH)
        }
        mainView.addGestureRecognizer(longPressGesture)
    }

    fun pushEvent(touches: Set<*>, inputEventType: InputEventType) {
        for (touch in touches) {
            touch as UITouch
            val point = touch.locationInView(touch.view).value
            pushEvent(point, inputEventType)
        }
    }

    private fun pushEvent(point: CGPoint, inputEventType: InputEventType) {
        val xInPixels = point.x.toFloat() * pixelsInPoint
        val yInPixels = point.y.toFloat() * pixelsInPoint
        input.addEvent(InputEvent(InputEventSource.LEFT, inputEventType, xInPixels, yInPixels, xInPixels, yInPixels))
    }

}
