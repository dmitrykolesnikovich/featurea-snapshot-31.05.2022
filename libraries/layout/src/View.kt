package featurea.layout

import featurea.math.Surface
import featurea.math.Coordinates
import featurea.math.Rectangle

open class View {

    var isVisible: Boolean = true
    var coordinates: Coordinates? = null
    val style = ViewStyle()
    var rectangle = Rectangle()
    lateinit var surface: Surface

}
