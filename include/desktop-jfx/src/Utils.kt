package featurea.desktop.jfx

import featurea.jvm.enumConstant
import featurea.jvm.method
import featurea.jvm.methodReturnType
import featurea.jvm.type
import javafx.application.Platform
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.*
import java.lang.Double.min
import java.lang.management.ManagementFactory
import java.lang.management.PlatformLoggingMXBean
import java.util.*
import javax.management.Attribute
import javax.management.ObjectName
import javax.swing.JComponent
import javafx.scene.control.CheckMenuItem as JfxCheckMenuItem
import javafx.scene.control.MenuItem as JfxMenuItem
import javafx.scene.input.KeyEvent as JfxKeyEvent
import javafx.scene.paint.Color as JfxColor

fun MenuItem(title: String?, action: () -> Unit = {}): JfxMenuItem = JfxMenuItem(title).apply {
    onAction = EventHandler {
        action()
    }
}

fun CheckMenuItem(title: String?, initalSelected: Boolean = false, action: (Boolean) -> Unit = {}): JfxCheckMenuItem =
    JfxCheckMenuItem(title).apply {
        isSelected = initalSelected
        onAction = EventHandler {
            action(isSelected)
        }
    }

fun JfxColor.toBackground(): Background = Background(BackgroundFill(this, null, null))

fun String.toImageOrNull(size: Int): Image? = toImageOrNull(width = size, height = size)

fun String.toImageOrNull(width: Int = 0, height: Int = 0): Image? {
    val inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(this) ?: return null
    return if (width == 0 && height == 0) {
        Image(inputStream)
    } else {
        Image(inputStream, width.toDouble(), height.toDouble(), true, true)
    }
}

fun String.toImageView(width: Int = 0, height: Int = 0): ImageView = ImageView(toImageOrNull(width, height))

fun GraphicsContext.clearCanvas() = clearRect(0.0, 0.0, canvas.width, canvas.height)

fun SplitPane.setStageSize(stage: Stage, size: IntRange) {
    setDividerPositions(size.first / stage.width)
    stage.maximizedProperty().addListener(ChangeListener { _, _, isMaximized ->
        if (isMaximized) {
            if (dividerPositions[0] < size.first / stage.width) setDividerPositions(size.first / stage.width)
            if (dividerPositions[0] > size.last / stage.width) setDividerPositions(size.last / stage.width)
        }
    })
}

fun createHueGradient(): LinearGradient {
    var offset: Double
    val stops = arrayOfNulls<Stop>(255)
    for (x in 0..254) {
        offset = 1.0 / 255 * x
        val h = (x / 255.0 * 360).toInt()
        stops[x] = Stop(offset, javafx.scene.paint.Color.hsb(h.toDouble(), 1.0, 1.0))
    }
    return LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, *stops)
}

fun JfxColor.toColorResource(): String = toString().replace("0x", "#").toUpperCase()

val Double.em: Double get() = 12.0 * this

val allFonts: Array<String> by lazy {
    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.US)
}
val cyrillicFonts: Array<String> by lazy {
    allFonts.filter { Font(it, Font.PLAIN, 8).canDisplay('Ю') }.toTypedArray()
}

fun Control.withLabel(label: String, gap: Double = 8.0): Node = GridPane().apply {
    hgap = gap
    addColumn(0, Label(label))
    addColumn(1, this@withLabel)
    this@withLabel.maxWidth(kotlin.Double.MAX_VALUE)
    this@withLabel.maxHeight(kotlin.Double.MAX_VALUE)
    GridPane.setFillWidth(this@withLabel, true)
    columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.NEVER })
    columnConstraints.add(ColumnConstraints().apply {
        hgrow = Priority.ALWAYS; isFillWidth = true; halignment = HPos.RIGHT // quickfix todo improve
    })
}

fun Control.withButton(button: String, gap: Double = 8.0, action: () -> Unit): Node = GridPane().apply {
    hgap = gap
    addColumn(0, this@withButton)
    addColumn(1, Button(button).apply {
        onAction = EventHandler {
            action()
        }
    })
    this@withButton.maxWidth(kotlin.Double.MAX_VALUE)
    this@withButton.maxHeight(kotlin.Double.MAX_VALUE)
    GridPane.setFillWidth(this@withButton, true)
    columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS; isFillWidth = true })
    columnConstraints.add(ColumnConstraints().apply {
        hgrow = Priority.NEVER; halignment = HPos.RIGHT// quickfix todo improve
    })
}

fun Node.fillWidth(halignment: HPos = HPos.LEFT, valignment: VPos = VPos.BOTTOM): Region = GridPane().apply {
    addColumn(0, this@fillWidth)
    columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS; this.halignment = halignment })
    rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS; this.valignment = valignment })
}

fun fillWidth(left: List<Node> = emptyList(), right: List<Node> = emptyList(), hgap: Double = 8.0): Node =
    GridPane().apply {
        this.hgap = hgap
        var column = 0
        if (left.isNotEmpty()) {
            for (leftNode in left) {
                addColumn(column++, leftNode)
                columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.NEVER; this.halignment = HPos.LEFT })
            }
        }
        if (right.isNotEmpty()) {
            right.first().also {
                addColumn(column++, it)
                columnConstraints.add(ColumnConstraints().apply {
                    hgrow = Priority.ALWAYS; this.halignment = HPos.RIGHT
                })
            }
            for (index in 1 until right.size) {
                addColumn(column++, right[index])
                columnConstraints.add(ColumnConstraints().apply {
                    hgrow = Priority.NEVER; this.halignment = HPos.RIGHT
                })
            }
        }
        rowConstraints.add(RowConstraints().apply {
            vgrow = Priority.ALWAYS; this.valignment = VPos.BOTTOM
        }) // todo improve hotfix
    }

// https://stackoverflow.com/a/48694320/909169
fun TextArea.hideScrollBar(): TextArea {
    stylesheets.add("featurea/jfx/text-area.css".externalPath)
    return this
}

// https://stackoverflow.com/a/47742620/909169
fun <T : Any> Spinner<T>.fixValueChange(): Spinner<T> = apply {
    isEditable = true
    editor.textProperty().onChange { text ->
        if (text.isNullOrBlank()) return@onChange
        valueFactory.value = try {
            valueFactory.converter.fromString(text)
        } catch (failure: Throwable) {
            value
        }
    }
}

fun JComponent.onMouseEvent(listener: MouseAdapter) {
    addMouseListener(object : MouseListener {
        override fun mouseReleased(event: MouseEvent) = Platform.runLater { listener.mouseReleased(event) }
        override fun mouseEntered(event: MouseEvent) = Platform.runLater { listener.mouseEntered(event) }
        override fun mouseClicked(event: MouseEvent) = Platform.runLater { listener.mouseClicked(event) }
        override fun mouseExited(event: MouseEvent) = Platform.runLater { listener.mouseExited(event) }
        override fun mousePressed(event: MouseEvent) = Platform.runLater { listener.mousePressed(event) }
    })
    addMouseMotionListener(object : MouseMotionListener {
        override fun mouseMoved(event: MouseEvent) = Platform.runLater { listener.mouseMoved(event) }
        override fun mouseDragged(event: MouseEvent) = Platform.runLater { listener.mouseDragged(event) }
    })
    addMouseWheelListener { event -> Platform.runLater { listener.mouseWheelMoved(event) } }
}

fun TableView<*>.isEditing(): Boolean = editingCell != null

fun confirmDialog(title: String?, message: String, action: () -> Unit): ButtonType {
    val alert = Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO)
    alert.initStyle(StageStyle.UTILITY)
    alert.headerText = null
    alert.title = title
    alert.graphic = null
    alert.showAndWait()
    if (alert.result == ButtonType.YES) {
        action()
    }
    return alert.result
}

fun warningDialog(title: String?, message: String) {
    val alert = Alert(Alert.AlertType.WARNING, message, ButtonType.OK)
    alert.initStyle(StageStyle.UTILITY)
    alert.headerText = null
    alert.title = title
    alert.graphic = null
    alert.showAndWait()
}

fun infoDialog(title: String, message: String, action: () -> Unit = {}) {
    Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).apply {
        initStyle(StageStyle.UTILITY)
        this.headerText = null
        this.title = title
        this.graphic = null
    }.also {
        it.showAndWait()
        if (it.result == ButtonType.OK) action()
    }
}

@Suppress("NewApi")
fun ImageView.centerImage() {
    if (image != null) {
        val ratioX: Double = fitWidth / image.width
        val ratioY: Double = fitHeight / image.height
        val ratio: Double = min(ratioX, ratioY)
        val width: Double = image.width * ratio
        val height: Double = image.height * ratio
        x = (fitWidth - width) / 2
        y = (fitHeight - height) / 2
    }
}

fun Window.closeOnEscape() {
    addEventFilter(JfxKeyEvent.KEY_PRESSED) { event ->
        if (event.code == KeyCode.ESCAPE) {
            hide()
        }
    }
}

fun Node.withAlignment(alignment: Pos, init: HBox.() -> Unit = {}): HBox =
    HBox().apply { children.add(this@withAlignment); this.alignment = alignment; init() }

fun <T> TableCell<T, String>.fireEditEvent(result: String) {
    val tablePosition = TablePosition(tableView, tableRow.index, tableColumn)
    val editEvent = TableColumn.CellEditEvent(tableView, tablePosition, TableColumn.editCommitEvent(), result)
    Event.fireEvent(tableColumn, editEvent)
}

fun findCpuLoad(): Double {
    val server = ManagementFactory.getPlatformMBeanServer()
    val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
    val attributes = server.getAttributes(name, arrayOf("ProcessCpuLoad"))
    if (attributes.isEmpty()) return Double.NaN
    val attribute = attributes[0] as Attribute
    val value = attribute.value as Double
    if (value == -1.0) {
        return Double.NaN
    } else {
        return (value * 1000).toInt() / 10.0
    }
}

// https://stackoverflow.com/a/48797514/909169
fun disableJfxLogger() {
    try {
        // com.sun.javafx.util.Logging.getCSSLogger().setLevel(sun.util.logging.PlatformLogger.Level.OFF)
        val logger = type("com.sun.javafx.util.Logging").method("getCSSLogger").invoke(null)
        val setLevel = type("com.sun.javafx.util.Logging").methodReturnType("getCSSLogger").method("setLevel")
        val levelOff = type("sun.util.logging.PlatformLogger\$Level").enumConstant("OFF")
        setLevel.invoke(logger, levelOff)
    } catch (skip: Throwable) {
        // no op
    }
    try {
        ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean::class.java).setLoggerLevel("javafx.css", "OFF")
    } catch (skip: Throwable) {
        // no op
    }
}

val String.externalPath: String? get() = ClassLoader.getSystemClassLoader().getResource(this)?.toExternalForm()

fun JfxMenuItem.onAction(block: () -> Unit) {
    onAction = EventHandler {
        block()
    }
}

fun <T : Tab> TabPane.findSelectedTabOrNull(): T? = selectionModel.selectedItem as T?

fun <T> TreeView<T>.updateTreeItems(init: (treeItem: TreeItem<T>) -> Unit) {
    fun <T> TreeItem<T>.updateRecursively(block: (treeItem: TreeItem<T>) -> Unit) {
        block(this)
        for (child in children) {
            child.updateRecursively(block)
        }
    }

    root.updateRecursively(init)
    refresh() // IMPORTANT do not remove this
}

fun Stage(owner: Window): Stage {
    return Stage().apply { initOwner(owner) }
}

fun hiddenProgressBar(): ProgressBar {
    return ProgressBar().apply {
        minWidth = 0.0
        maxWidth = 0.0
        prefWidth = 0.0
        minHeight = 0.0
        maxHeight = 0.0
        prefHeight = 0.0
    }
}

var Node.resizableWithParent: Boolean
    get() = properties["resizable-with-parent"] as Boolean
    set(value) {
        properties["resizable-with-parent"] = value
    }

fun FileChooser(vararg extensionFilters: FileChooser.ExtensionFilter): FileChooser =
    javafx.stage.FileChooser().apply { this.extensionFilters.addAll(extensionFilters) }

fun Tab(content: Node): Tab {
    return javafx.scene.control.Tab().apply {
        this.content = content
    }
}
