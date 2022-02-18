package featurea.desktop.jfx

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop

// https://stackoverflow.com/a/27180647/909169
class FSColorPicker : VBox() {

    val currentColorProperty: ObjectProperty<Color> = SimpleObjectProperty(Color.WHITE)
    private var currentColor: Color
        get() = currentColorProperty.get()
        set(currentColor) {
            this.currentColorProperty.set(currentColor)
            updateValues()
        }

    val customColorProperty: ObjectProperty<Color> = SimpleObjectProperty(Color.TRANSPARENT)
    private var customColor: Color
        get() = customColorProperty.get()
        set(color) = customColorProperty.set(color)

    private val colorRect: Pane
    private val colorBar: Pane
    private val colorRectOverlayOne: Pane
    private val colorRectOverlayTwo: Pane
    private val colorRectIndicator: Region
    private val colorBarIndicator: Region
    private val newColorRect: Pane

    private val hue = SimpleDoubleProperty(-1.0)
    private val sat = SimpleDoubleProperty(-1.0)
    private val bright = SimpleDoubleProperty(-1.0)
    private val alpha = object : SimpleDoubleProperty(100.0) {
        override fun invalidated() {
            customColor = Color(
                customColor.red, customColor.green, customColor.blue,
                clamp(get() / 100)
            )
        }
    }

    val rgbaTextField = TextField()

    init {
        styleClass.add("my-custom-color")

        val box = VBox().apply { styleClass.add("color-rect-pane") }

        customColorProperty.onChange { colorChanged() }
        colorRectIndicator = Region().apply {
            id = "color-rect-indicator"
            isManaged = false
            isMouseTransparent = true
            isCache = true
        }


        val colorRectOpacityContainer = StackPane()

        colorRect = StackPane()
        colorRect.getStyleClass().addAll("color-rect", "transparent-pattern")

        val colorRectHue = Pane().apply {
            backgroundProperty().bind(object : ObjectBinding<Background>() {
                init {
                    bind(hue)
                }

                override fun computeValue(): Background {
                    return Background(BackgroundFill(Color.hsb(hue.value!!, 1.0, 1.0), CornerRadii.EMPTY, Insets.EMPTY))
                }
            })
        }

        colorRectOverlayOne = Pane().apply {
            styleClass.add("color-rect")
            background = Background(
                BackgroundFill(
                    LinearGradient(
                        0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
                        Stop(0.0, Color.rgb(255, 255, 255, 1.0)), Stop(1.0, Color.rgb(255, 255, 255, 0.0))
                    ), CornerRadii.EMPTY, Insets.EMPTY
                )
            )
        }

        val rectMouseHandler = EventHandler<MouseEvent> { event ->
            sat.set(clamp(event.x / colorRect.width) * 100)
            bright.set(100 - clamp(event.y / colorRect.height) * 100)
            updateHSBColor()
        }

        colorRectOverlayTwo = Pane().apply {
            styleClass.addAll("color-rect")
            background = Background(
                BackgroundFill(
                    LinearGradient(
                        0.0, 0.0, 0.0, 1.0, true,
                        CycleMethod.NO_CYCLE, Stop(0.0, Color.rgb(0, 0, 0, 0.0)), Stop(1.0, Color.rgb(0, 0, 0, 1.0))
                    ),
                    CornerRadii.EMPTY, Insets.EMPTY
                )
            )
            onMouseDragged = rectMouseHandler
            onMousePressed = rectMouseHandler
        }


        val colorRectBlackBorder = Pane().apply {
            isMouseTransparent = true
            styleClass.addAll("color-rect", "color-rect-border")
        }

        colorBar = Pane().apply {
            styleClass.add("color-bar")
            background = Background(BackgroundFill(createHueGradient(), CornerRadii.EMPTY, Insets.EMPTY))
        }


        colorBarIndicator = Region().apply {
            id = "color-bar-indicator"
            isMouseTransparent = true
            isCache = true
        }


        colorRectIndicator.layoutXProperty().bind(sat.divide(100).multiply(colorRect.widthProperty()))
        colorRectIndicator.layoutYProperty()
            .bind(Bindings.subtract(1, bright.divide(100)).multiply(colorRect.heightProperty()))
        colorBarIndicator.layoutXProperty().bind(hue.divide(360).multiply(colorBar.widthProperty()))
        colorRectOpacityContainer.opacityProperty().bind(alpha.divide(100))

        val barMouseHandler = EventHandler<MouseEvent> { event ->
            val x = event.getX()
            hue.set(clamp(x / colorRect.width) * 360)
            updateHSBColor()
        }

        colorBar.onMouseDragged = barMouseHandler
        colorBar.onMousePressed = barMouseHandler

        newColorRect = Pane().apply {
            styleClass.add("color-new-rect")
            id = "new-color"
            backgroundProperty().bind(object : ObjectBinding<Background>() {
                init {
                    bind(customColorProperty)
                }

                override fun computeValue(): Background {
                    return Background(BackgroundFill(customColorProperty.get(), CornerRadii.EMPTY, Insets.EMPTY))
                }
            })
        }

        colorBar.children.setAll(colorBarIndicator)
        colorRectOpacityContainer.children.setAll(colorRectHue, colorRectOverlayOne, colorRectOverlayTwo)
        colorRect.getChildren().setAll(colorRectOpacityContainer, colorRectBlackBorder, colorRectIndicator)
        setVgrow(colorRect, Priority.SOMETIMES)

        box.children.addAll(colorBar, colorRect, newColorRect, rgbaTextField.withLabel("RGBA:"))
        children.add(box)
        if (currentColorProperty.get() == null) currentColorProperty.set(Color.TRANSPARENT)
        updateValues()
    }

    override fun layoutChildren() {
        super.layoutChildren()
        colorRectIndicator.autosize()
    }

    /*internals*/

    private fun updateValues() {
        hue.set(currentColor.hue)
        sat.set(currentColor.saturation * 100)
        bright.set(currentColor.brightness * 100)
        alpha.set(currentColor.opacity * 100)
        customColor = Color.hsb(
            hue.get(),
            clamp(sat.get() / 100),
            clamp(bright.get() / 100),
            clamp(alpha.get() / 100)
        )
    }

    private fun colorChanged() {
        hue.set(customColor.hue)
        sat.set(customColor.saturation * 100)
        bright.set(customColor.brightness * 100)
    }

    private fun updateHSBColor() {
        customColor = Color.hsb(
            hue.get(),
            clamp(sat.get() / 100),
            clamp(bright.get() / 100),
            clamp(alpha.get() / 100)
        )
    }

}

/*internals*/

private fun clamp(value: Double, min: Double = 0.0, max: Double = 1.0): Double =
    if (value < min) min else if (value > max) max else value

private fun clamp(value: Float, min: Float = 0f, max: Float = 1f): Float =
    if (value < min) min else if (value > max) max else value
