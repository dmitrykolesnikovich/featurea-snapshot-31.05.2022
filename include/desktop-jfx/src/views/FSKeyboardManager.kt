package featurea.desktop.jfx

import javafx.event.EventHandler
import javafx.scene.input.KeyCode.SHIFT
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import javafx.stage.Window
import java.awt.event.MouseEvent.BUTTON2

var isShiftKeyPressed: Boolean = false
    private set
var isShortcutKeyPressed: Boolean = false
    private set

fun onKeyEvent(action: (event: KeyEvent) -> Unit) = registeredActions.add(EventHandler { action(it) })

private val registeredStages = mutableSetOf<Stage>()
private val registeredActions = mutableListOf<EventHandler<KeyEvent>>()

fun Stage.registerGlobalKeyEvents(owner: Window? = null) = apply {
    val stage = this
    if (registeredStages.add(stage)) {
        addEventFilter(KeyEvent.KEY_PRESSED) {
            when (it.code) {
                SHIFT -> isShiftKeyPressed = true
            }
            isShortcutKeyPressed = it.isShortcutDown
            for (action in registeredActions) action.handle(it)
        }
        addEventFilter(KeyEvent.KEY_RELEASED) {
            when (it.code) {
                SHIFT -> isShiftKeyPressed = false
            }
            isShortcutKeyPressed = it.isShortcutDown
        }
    }
    // >> quickfix todo extract to separate function
    if (owner != null) {
        stage.initOwner(owner)
        onShowing = EventHandler {
            stage.x = owner.x + owner.width + 2
            stage.y = owner.y
        }
    }
    // <<
}

val Int.isWheelButton: Boolean get() = this == BUTTON2
