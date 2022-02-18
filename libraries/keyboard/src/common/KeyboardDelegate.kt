package featurea.keyboard

import featurea.runtime.Component
import featurea.runtime.Module

expect class KeyboardDelegate(module: Module) : Component{
    fun show(keyboardType: KeyboardType)
    fun hide()
}