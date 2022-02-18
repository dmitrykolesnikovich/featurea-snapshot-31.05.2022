package featurea.ios

import kotlinx.cinterop.CValue
import kotlinx.cinterop.memScoped
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.QuartzCore.CALayer
import platform.UIKit.*

val pixelsInPoint: Float = UIScreen.mainScreen.scale.toFloat() // todo move to common source set

operator fun CGSize.component1() = width

operator fun CGSize.component2() = height

val CGRectEmpty: CValue<CGRect> = CGRectMake(0.0, 0.0, 0.0, 0.0)

val UIView.size: CGSize get() = frame.value.size

interface UIViewControllerListener {
    fun viewWillTransitionToSize(viewController: UIViewController, size: CGSize) {}
}

class UIApplicationDelegate {
    val viewControllerListeners = mutableListOf<UIViewControllerListener>()
    var interfaceOrientationMask: UIInterfaceOrientationMask = UIInterfaceOrientationMaskAll
}

class HeaderView : UIView(CGRectEmpty)

fun UITextField.setupKeyboard(keyboardType: UIKeyboardType, returnKeyType: UIReturnKeyType) {
    setAutocapitalizationType(UITextAutocapitalizationType.UITextAutocapitalizationTypeNone)
    setAutocorrectionType(UITextAutocorrectionType.UITextAutocorrectionTypeNo)
    setSpellCheckingType(UITextSpellCheckingType.UITextSpellCheckingTypeNo)
    setKeyboardAppearance(UIKeyboardAppearanceDefault)
    setReturnKeyType(returnKeyType)
    setClearButtonMode(UITextFieldViewMode.UITextFieldViewModeAlways)
    setKeyboardType(keyboardType)
    setDelegate(null)
}

fun UITextField.setupBorderBottomLine(color: UIColor, depth: Double) {
    val bottomLine = CALayer()
    val (width, height) = frame.value.size
    println("setupBorderBottomLine: $width, $height")
    bottomLine.frame = CGRectMake(0.0, height - depth, width, depth)
    bottomLine.backgroundColor = color.CGColor
    borderStyle = UITextBorderStyle.UITextBorderStyleNone
    layer.addSublayer(bottomLine)
}
