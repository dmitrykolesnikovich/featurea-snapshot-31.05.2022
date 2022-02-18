package featurea.keyboard

import java.awt.event.KeyEvent as SwingKeyEvent

fun findKeyEventSourceFromSwingKeyEvent(event: SwingKeyEvent): KeyEventSource {
    if (event.keyChar == ':') return KeyEventSource.COLON // quickfix todo improve
    when (event.keyCode) {
        SwingKeyEvent.VK_ADD -> return KeyEventSource.PLUS
        SwingKeyEvent.VK_SUBTRACT -> return KeyEventSource.MINUS
        SwingKeyEvent.VK_0 -> return KeyEventSource.NUM_0
        SwingKeyEvent.VK_1 -> return KeyEventSource.NUM_1
        SwingKeyEvent.VK_2 -> return KeyEventSource.NUM_2
        SwingKeyEvent.VK_3 -> return KeyEventSource.NUM_3
        SwingKeyEvent.VK_4 -> return KeyEventSource.NUM_4
        SwingKeyEvent.VK_5 -> return KeyEventSource.NUM_5
        SwingKeyEvent.VK_6 -> return KeyEventSource.NUM_6
        SwingKeyEvent.VK_7 -> return KeyEventSource.NUM_7
        SwingKeyEvent.VK_8 -> return KeyEventSource.NUM_8
        SwingKeyEvent.VK_9 -> return KeyEventSource.NUM_9
        SwingKeyEvent.VK_A -> return KeyEventSource.A
        SwingKeyEvent.VK_B -> return KeyEventSource.B
        SwingKeyEvent.VK_C -> return KeyEventSource.C
        SwingKeyEvent.VK_D -> return KeyEventSource.D
        SwingKeyEvent.VK_E -> return KeyEventSource.E
        SwingKeyEvent.VK_F -> return KeyEventSource.F
        SwingKeyEvent.VK_G -> return KeyEventSource.G
        SwingKeyEvent.VK_H -> return KeyEventSource.H
        SwingKeyEvent.VK_I -> return KeyEventSource.I
        SwingKeyEvent.VK_J -> return KeyEventSource.J
        SwingKeyEvent.VK_K -> return KeyEventSource.K
        SwingKeyEvent.VK_L -> return KeyEventSource.L
        SwingKeyEvent.VK_M -> return KeyEventSource.M
        SwingKeyEvent.VK_N -> return KeyEventSource.N
        SwingKeyEvent.VK_O -> return KeyEventSource.O
        SwingKeyEvent.VK_P -> return KeyEventSource.P
        SwingKeyEvent.VK_Q -> return KeyEventSource.Q
        SwingKeyEvent.VK_R -> return KeyEventSource.R
        SwingKeyEvent.VK_S -> return KeyEventSource.S
        SwingKeyEvent.VK_T -> return KeyEventSource.T
        SwingKeyEvent.VK_U -> return KeyEventSource.U
        SwingKeyEvent.VK_V -> return KeyEventSource.V
        SwingKeyEvent.VK_W -> return KeyEventSource.W
        SwingKeyEvent.VK_X -> return KeyEventSource.X
        SwingKeyEvent.VK_Y -> return KeyEventSource.Y
        SwingKeyEvent.VK_Z -> return KeyEventSource.Z
        SwingKeyEvent.VK_ALT -> return KeyEventSource.ALT_LEFT
        SwingKeyEvent.VK_ALT_GRAPH -> return KeyEventSource.ALT_RIGHT
        SwingKeyEvent.VK_BACK_SLASH -> return KeyEventSource.BACKSLASH
        SwingKeyEvent.VK_COMMA -> return KeyEventSource.COMMA
        SwingKeyEvent.VK_DELETE -> return KeyEventSource.DELETE
        SwingKeyEvent.VK_LEFT -> return KeyEventSource.DPAD_LEFT
        SwingKeyEvent.VK_RIGHT -> return KeyEventSource.DPAD_RIGHT
        SwingKeyEvent.VK_UP -> return KeyEventSource.DPAD_UP
        SwingKeyEvent.VK_DOWN -> return KeyEventSource.DPAD_DOWN
        SwingKeyEvent.VK_ENTER -> return KeyEventSource.ENTER
        SwingKeyEvent.VK_HOME -> return KeyEventSource.HOME
        SwingKeyEvent.VK_MINUS -> return KeyEventSource.MINUS
        SwingKeyEvent.VK_PERIOD -> return KeyEventSource.PERIOD
        SwingKeyEvent.VK_PLUS -> return KeyEventSource.PLUS
        SwingKeyEvent.VK_SEMICOLON -> return KeyEventSource.SEMICOLON
        SwingKeyEvent.VK_SHIFT -> return KeyEventSource.SHIFT_LEFT
        SwingKeyEvent.VK_SLASH -> return KeyEventSource.SLASH
        SwingKeyEvent.VK_SPACE -> return KeyEventSource.SPACE
        SwingKeyEvent.VK_TAB -> return KeyEventSource.TAB
        SwingKeyEvent.VK_BACK_SPACE -> return KeyEventSource.BACKSPACE
        SwingKeyEvent.VK_CONTROL -> return KeyEventSource.CONTROL_LEFT
        SwingKeyEvent.VK_ESCAPE -> return KeyEventSource.ESCAPE
        SwingKeyEvent.VK_END -> return KeyEventSource.END
        SwingKeyEvent.VK_INSERT -> return KeyEventSource.INSERT
        SwingKeyEvent.VK_PAGE_UP -> return KeyEventSource.PAGE_UP
        SwingKeyEvent.VK_PAGE_DOWN -> return KeyEventSource.PAGE_DOWN
        SwingKeyEvent.VK_F1 -> return KeyEventSource.F1
        SwingKeyEvent.VK_F2 -> return KeyEventSource.F2
        SwingKeyEvent.VK_F3 -> return KeyEventSource.F3
        SwingKeyEvent.VK_F4 -> return KeyEventSource.F4
        SwingKeyEvent.VK_F5 -> return KeyEventSource.F5
        SwingKeyEvent.VK_F6 -> return KeyEventSource.F6
        SwingKeyEvent.VK_F7 -> return KeyEventSource.F7
        SwingKeyEvent.VK_F8 -> return KeyEventSource.F8
        SwingKeyEvent.VK_F9 -> return KeyEventSource.F9
        SwingKeyEvent.VK_F10 -> return KeyEventSource.F10
        SwingKeyEvent.VK_F11 -> return KeyEventSource.F11
        SwingKeyEvent.VK_F12 -> return KeyEventSource.F12
        SwingKeyEvent.VK_COLON -> return KeyEventSource.COLON
        SwingKeyEvent.VK_NUMPAD0 -> return KeyEventSource.NUM_0
        SwingKeyEvent.VK_NUMPAD1 -> return KeyEventSource.NUM_1
        SwingKeyEvent.VK_NUMPAD2 -> return KeyEventSource.NUM_2
        SwingKeyEvent.VK_NUMPAD3 -> return KeyEventSource.NUM_3
        SwingKeyEvent.VK_NUMPAD4 -> return KeyEventSource.NUM_4
        SwingKeyEvent.VK_NUMPAD5 -> return KeyEventSource.NUM_5
        SwingKeyEvent.VK_NUMPAD6 -> return KeyEventSource.NUM_6
        SwingKeyEvent.VK_NUMPAD7 -> return KeyEventSource.NUM_7
        SwingKeyEvent.VK_NUMPAD8 -> return KeyEventSource.NUM_8
        SwingKeyEvent.VK_NUMPAD9 -> return KeyEventSource.NUM_9
        else -> return KeyEventSource.UNKNOWN
    }
}
