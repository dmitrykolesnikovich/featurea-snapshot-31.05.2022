package featurea.ios

import kotlinx.cinterop.*
import platform.Foundation.NSSelectorFromString
import platform.Foundation.addObserver
import platform.QuartzCore.CADisplayLink
import platform.UIKit.UIControl
import platform.UIKit.UIControlEvents
import platform.UIKit.UILongPressGestureRecognizer
import platform.darwin.NSObject
import platform.darwin.sel_registerName

// https://discuss.kotlinlang.org/t/how-to-call-a-selector-from-kotlin-for-ios/4591/4

fun NSObject.addObserver(keyPath: String, action: () -> Unit) = addObserver(action.target, keyPath, 0u, action.selector)

fun CADisplayLink.Companion.displayLinkWithTarget(action: () -> Unit) =
    CADisplayLink.displayLinkWithTarget(action.target, action.selector)

fun UILongPressGestureRecognizer.addTarget(action: () -> Unit) = addTarget(action.target, action.selector)

fun UIControl.addTarget(controlEvents: UIControlEvents, action: () -> Unit) =
    addTarget(action.target, action.selector, controlEvents)

/*internals*/

val (() -> Unit).selector get() = NSSelectorFromString("action")

val (() -> Unit).target
    get() = object : NSObject() {
        @ObjCAction
        fun action() = invoke()
    }
