package featurea.keyboard

import featurea.js.isUserAgentMobile
import featurea.keyboard.KeyMapping.*
import org.w3c.dom.events.KeyboardEvent

private enum class KeyMapping(val keyCode: Int) {
    DOM_VK_CANCEL(0x03),
    DOM_VK_HELP(0x06),
    DOM_VK_BACK_SPACE(0x08),
    DOM_VK_TAB(0x09),
    DOM_VK_CLEAR(0x0C),
    DOM_VK_RETURN(0x0D),
    DOM_VK_ENTER(0x0E),
    DOM_VK_SHIFT(0x10),
    DOM_VK_CONTROL(0x11),
    DOM_VK_ALT(0x12),
    DOM_VK_PAUSE(0x13),
    DOM_VK_CAPS_LOCK(0x14),
    DOM_VK_KANA(0x15),
    DOM_VK_HANGUL(0x15),
    DOM_VK_EISU(0x16),
    DOM_VK_JUNJA(0x17),
    DOM_VK_FINAL(0x18),
    DOM_VK_HANJA(0x19),
    DOM_VK_KANJI(0x19),
    DOM_VK_ESCAPE(0x1B),
    DOM_VK_CONVERT(0x1C),
    DOM_VK_NONCONVERT(0x1D),
    DOM_VK_ACCEPT(0x1E),
    DOM_VK_MODECHANGE(0x1F),
    DOM_VK_SPACE(0x20),
    DOM_VK_PAGE_UP(0x21),
    DOM_VK_PAGE_DOWN(0x22),
    DOM_VK_END(0x23),
    DOM_VK_HOME(0x24),
    DOM_VK_LEFT(0x25),
    DOM_VK_UP(0x26),
    DOM_VK_RIGHT(0x27),
    DOM_VK_DOWN(0x28),
    DOM_VK_SELECT(0x29),
    DOM_VK_PRINT(0x2A),
    DOM_VK_EXECUTE(0x2B),
    DOM_VK_PRINTSCREEN(0x2C),
    DOM_VK_INSERT(0x2D),
    DOM_VK_DELETE(0x2E),
    DOM_VK_0(0x30),
    DOM_VK_1(0x31),
    DOM_VK_2(0x32),
    DOM_VK_3(0x33),
    DOM_VK_4(0x34),
    DOM_VK_5(0x35),
    DOM_VK_6(0x36),
    DOM_VK_7(0x37),
    DOM_VK_8(0x38),
    DOM_VK_9(0x39),
    DOM_VK_COLON(0x3A),
    DOM_VK_SEMICOLON(0x3B),
    DOM_VK_LESS_THAN(0x3C),
    DOM_VK_EQUALS(0x3D),
    DOM_VK_GREATER_THAN(0x3E),
    DOM_VK_QUESTION_MARK(0x3F),
    DOM_VK_AT(0x40),
    DOM_VK_A(0x41),
    DOM_VK_B(0x42),
    DOM_VK_C(0x43),
    DOM_VK_D(0x44),
    DOM_VK_E(0x45),
    DOM_VK_F(0x46),
    DOM_VK_G(0x47),
    DOM_VK_H(0x48),
    DOM_VK_I(0x49),
    DOM_VK_J(0x4A),
    DOM_VK_K(0x4B),
    DOM_VK_L(0x4C),
    DOM_VK_M(0x4D),
    DOM_VK_N(0x4E),
    DOM_VK_O(0x4F),
    DOM_VK_P(0x50),
    DOM_VK_Q(0x51),
    DOM_VK_R(0x52),
    DOM_VK_S(0x53),
    DOM_VK_T(0x54),
    DOM_VK_U(0x55),
    DOM_VK_V(0x56),
    DOM_VK_W(0x57),
    DOM_VK_X(0x58),
    DOM_VK_Y(0x59),
    DOM_VK_Z(0x5A),
    DOM_VK_WIN(0x5B),
    DOM_VK_CONTEXT_MENU(0x5D),
    DOM_VK_SLEEP(0x5F),
    DOM_VK_NUMPAD0(0x60),
    DOM_VK_NUMPAD1(0x61),
    DOM_VK_NUMPAD2(0x62),
    DOM_VK_NUMPAD3(0x63),
    DOM_VK_NUMPAD4(0x64),
    DOM_VK_NUMPAD5(0x65),
    DOM_VK_NUMPAD6(0x66),
    DOM_VK_NUMPAD7(0x67),
    DOM_VK_NUMPAD8(0x68),
    DOM_VK_NUMPAD9(0x69),
    DOM_VK_MULTIPLY(0x6A),
    DOM_VK_ADD(0x6B),
    DOM_VK_SEPARATOR(0x6C),
    DOM_VK_SUBTRACT(0x6D),
    DOM_VK_DECIMAL(0x6E),
    DOM_VK_DIVIDE(0x6F),
    DOM_VK_F1(0x70),
    DOM_VK_F2(0x71),
    DOM_VK_F3(0x72),
    DOM_VK_F4(0x73),
    DOM_VK_F5(0x74),
    DOM_VK_F6(0x75),
    DOM_VK_F7(0x76),
    DOM_VK_F8(0x77),
    DOM_VK_F9(0x78),
    DOM_VK_F10(0x79),
    DOM_VK_F11(0x7A),
    DOM_VK_F12(0x7B),
    DOM_VK_F13(0x7C),
    DOM_VK_F14(0x7D),
    DOM_VK_F15(0x7E),
    DOM_VK_F16(0x7F),
    DOM_VK_F17(0x80),
    DOM_VK_F18(0x81),
    DOM_VK_F19(0x82),
    DOM_VK_F20(0x83),
    DOM_VK_F21(0x84),
    DOM_VK_F22(0x85),
    DOM_VK_F23(0x86),
    DOM_VK_F24(0x87),
    DOM_VK_NUM_LOCK(0x90),
    DOM_VK_SCROLL_LOCK(0x91),
    DOM_VK_WIN_OEM_FJ_JISHO(0x92),
    DOM_VK_WIN_OEM_FJ_MASSHOU(0x93),
    DOM_VK_WIN_OEM_FJ_TOUROKU(0x94),
    DOM_VK_WIN_OEM_FJ_LOYA(0x95),
    DOM_VK_WIN_OEM_FJ_ROYA(0x96),
    DOM_VK_CIRCUMFLEX(0xA0),
    DOM_VK_EXCLAMATION(0xA1),
    DOM_VK_DOUBLE_QUOTE(0xA3),
    DOM_VK_HASH(0xA3),
    DOM_VK_DOLLAR(0xA4),
    DOM_VK_PERCENT(0xA5),
    DOM_VK_AMPERSAND(0xA6),
    DOM_VK_UNDERSCORE(0xA7),
    DOM_VK_OPEN_PAREN(0xA8),
    DOM_VK_CLOSE_PAREN(0xA9),
    DOM_VK_ASTERISK(0xAA),
    DOM_VK_PLUS(0xAB),
    DOM_VK_PIPE(0xAC),
    DOM_VK_HYPHEN_MINUS(0xAD),
    DOM_VK_OPEN_CURLY_BRACKET(0xAE),
    DOM_VK_CLOSE_CURLY_BRACKET(0xAF),
    DOM_VK_TILDE(0xB0),
    DOM_VK_VOLUME_MUTE(0xB5),
    DOM_VK_VOLUME_DOWN(0xB6),
    DOM_VK_VOLUME_UP(0xB7),
    DOM_VK_COMMA(0xBC),
    DOM_VK_PERIOD(0xBE),
    DOM_VK_SLASH(0xBF),
    DOM_VK_BACK_QUOTE(0xC0),
    DOM_VK_OPEN_BRACKET(0xDB),
    DOM_VK_BACK_SLASH(0xDC),
    DOM_VK_CLOSE_BRACKET(0xDD),
    DOM_VK_QUOTE(0xDE),
    DOM_VK_META(0xE0),
    DOM_VK_ALTGR(0xE1),
    DOM_VK_WIN_ICO_HELP(0xE3),
    DOM_VK_WIN_ICO_00(0xE4),
    DOM_VK_WIN_ICO_CLEAR(0xE6),
    DOM_VK_WIN_OEM_RESET(0xE9),
    DOM_VK_WIN_OEM_JUMP(0xEA),
    DOM_VK_WIN_OEM_PA1(0xEB),
    DOM_VK_WIN_OEM_PA2(0xEC),
    DOM_VK_WIN_OEM_PA3(0xED),
    DOM_VK_WIN_OEM_WSCTRL(0xEE),
    DOM_VK_WIN_OEM_CUSEL(0xEF),
    DOM_VK_WIN_OEM_ATTN(0xF0),
    DOM_VK_WIN_OEM_FINISH(0xF1),
    DOM_VK_WIN_OEM_COPY(0xF2),
    DOM_VK_WIN_OEM_AUTO(0xF3),
    DOM_VK_WIN_OEM_ENLW(0xF4),
    DOM_VK_WIN_OEM_BACKTAB(0xF5),
    DOM_VK_ATTN(0xF6),
    DOM_VK_CRSEL(0xF7),
    DOM_VK_EXSEL(0xF8),
    DOM_VK_EREOF(0xF9),
    DOM_VK_PLAY(0xFA),
    DOM_VK_ZOOM(0xFB),
    DOM_VK_PA1(0xFD),
    DOM_VK_WIN_OEM_CLEAR(0xFE)
}

private fun KeyboardEvent.findKeyMapping(): KeyMapping? {
    if (keyCode == 186) return DOM_VK_COLON // quickfix todo improve
    for (value in KeyMapping.values()) if (value.keyCode == keyCode) return value
    return null
}

fun KeyboardEvent.findKeyEventSourceOrNull(): KeyEventSource? {
    if (isUserAgentMobile && key == "Unidentified" && keyCode == 229) {
        return null
    }
    val keyMapping = findKeyMapping()
    return when (keyMapping) {
        DOM_VK_CANCEL -> KeyEventSource.UNKNOWN
        DOM_VK_HELP -> KeyEventSource.UNKNOWN
        DOM_VK_BACK_SPACE -> KeyEventSource.BACKSPACE
        DOM_VK_TAB -> KeyEventSource.TAB
        DOM_VK_CLEAR -> KeyEventSource.CLEAR
        DOM_VK_RETURN -> KeyEventSource.ENTER
        DOM_VK_ENTER -> KeyEventSource.UNKNOWN
        DOM_VK_SHIFT -> KeyEventSource.SHIFT_LEFT
        DOM_VK_CONTROL -> KeyEventSource.CONTROL_LEFT
        DOM_VK_ALT -> KeyEventSource.ALT_LEFT
        DOM_VK_PAUSE -> KeyEventSource.MEDIA_PLAY_PAUSE
        DOM_VK_CAPS_LOCK -> KeyEventSource.CAPS_LOCK
        DOM_VK_KANA -> KeyEventSource.UNKNOWN
        DOM_VK_HANGUL -> KeyEventSource.UNKNOWN
        DOM_VK_EISU -> KeyEventSource.UNKNOWN
        DOM_VK_JUNJA -> KeyEventSource.UNKNOWN
        DOM_VK_FINAL -> KeyEventSource.UNKNOWN
        DOM_VK_HANJA -> KeyEventSource.UNKNOWN
        DOM_VK_KANJI -> KeyEventSource.UNKNOWN
        DOM_VK_ESCAPE -> KeyEventSource.ESCAPE
        DOM_VK_CONVERT -> KeyEventSource.UNKNOWN
        DOM_VK_NONCONVERT -> KeyEventSource.UNKNOWN
        DOM_VK_ACCEPT -> KeyEventSource.UNKNOWN
        DOM_VK_MODECHANGE -> KeyEventSource.UNKNOWN
        DOM_VK_SPACE -> KeyEventSource.SPACE
        DOM_VK_PAGE_UP -> KeyEventSource.PAGE_UP
        DOM_VK_PAGE_DOWN -> KeyEventSource.PAGE_DOWN
        DOM_VK_END -> KeyEventSource.END
        DOM_VK_HOME -> KeyEventSource.HOME
        DOM_VK_LEFT -> KeyEventSource.DPAD_LEFT
        DOM_VK_UP -> KeyEventSource.DPAD_UP
        DOM_VK_RIGHT -> KeyEventSource.DPAD_RIGHT
        DOM_VK_DOWN -> KeyEventSource.DPAD_DOWN
        DOM_VK_SELECT -> KeyEventSource.BUTTON_SELECT
        DOM_VK_PRINT -> KeyEventSource.UNKNOWN
        DOM_VK_EXECUTE -> KeyEventSource.UNKNOWN
        DOM_VK_PRINTSCREEN -> KeyEventSource.UNKNOWN
        DOM_VK_INSERT -> KeyEventSource.INSERT
        DOM_VK_DELETE -> KeyEventSource.DELETE
        DOM_VK_0 -> KeyEventSource.NUMPAD_0
        DOM_VK_1 -> KeyEventSource.NUMPAD_1
        DOM_VK_2 -> KeyEventSource.NUMPAD_2
        DOM_VK_3 -> KeyEventSource.NUMPAD_3
        DOM_VK_4 -> KeyEventSource.NUMPAD_4
        DOM_VK_5 -> KeyEventSource.NUMPAD_5
        DOM_VK_6 -> KeyEventSource.NUMPAD_6
        DOM_VK_7 -> KeyEventSource.NUMPAD_7
        DOM_VK_8 -> KeyEventSource.NUMPAD_8
        DOM_VK_9 -> KeyEventSource.NUMPAD_9
        DOM_VK_COLON -> KeyEventSource.COLON
        DOM_VK_SEMICOLON -> KeyEventSource.SEMICOLON
        DOM_VK_LESS_THAN -> KeyEventSource.UNKNOWN
        DOM_VK_EQUALS -> KeyEventSource.EQUALS
        DOM_VK_GREATER_THAN -> KeyEventSource.UNKNOWN
        DOM_VK_QUESTION_MARK -> KeyEventSource.UNKNOWN
        DOM_VK_AT -> KeyEventSource.AT
        DOM_VK_A -> KeyEventSource.A
        DOM_VK_B -> KeyEventSource.B
        DOM_VK_C -> KeyEventSource.C
        DOM_VK_D -> KeyEventSource.D
        DOM_VK_E -> KeyEventSource.E
        DOM_VK_F -> KeyEventSource.F
        DOM_VK_G -> KeyEventSource.G
        DOM_VK_H -> KeyEventSource.H
        DOM_VK_I -> KeyEventSource.I
        DOM_VK_J -> KeyEventSource.J
        DOM_VK_K -> KeyEventSource.K
        DOM_VK_L -> KeyEventSource.L
        DOM_VK_M -> KeyEventSource.M
        DOM_VK_N -> KeyEventSource.N
        DOM_VK_O -> KeyEventSource.O
        DOM_VK_P -> KeyEventSource.P
        DOM_VK_Q -> KeyEventSource.Q
        DOM_VK_R -> KeyEventSource.R
        DOM_VK_S -> KeyEventSource.S
        DOM_VK_T -> KeyEventSource.T
        DOM_VK_U -> KeyEventSource.U
        DOM_VK_V -> KeyEventSource.V
        DOM_VK_W -> KeyEventSource.W
        DOM_VK_X -> KeyEventSource.X
        DOM_VK_Y -> KeyEventSource.Y
        DOM_VK_Z -> KeyEventSource.Z
        DOM_VK_WIN -> KeyEventSource.UNKNOWN
        DOM_VK_CONTEXT_MENU -> KeyEventSource.UNKNOWN
        DOM_VK_SLEEP -> KeyEventSource.UNKNOWN
        DOM_VK_NUMPAD0 -> KeyEventSource.NUMPAD_0
        DOM_VK_NUMPAD1 -> KeyEventSource.NUMPAD_1
        DOM_VK_NUMPAD2 -> KeyEventSource.NUMPAD_2
        DOM_VK_NUMPAD3 -> KeyEventSource.NUMPAD_3
        DOM_VK_NUMPAD4 -> KeyEventSource.NUMPAD_4
        DOM_VK_NUMPAD5 -> KeyEventSource.NUMPAD_5
        DOM_VK_NUMPAD6 -> KeyEventSource.NUMPAD_6
        DOM_VK_NUMPAD7 -> KeyEventSource.NUMPAD_7
        DOM_VK_NUMPAD8 -> KeyEventSource.NUMPAD_8
        DOM_VK_NUMPAD9 -> KeyEventSource.NUMPAD_9
        DOM_VK_MULTIPLY -> KeyEventSource.UNKNOWN
        DOM_VK_ADD -> KeyEventSource.PLUS
        DOM_VK_SEPARATOR -> KeyEventSource.UNKNOWN
        DOM_VK_SUBTRACT -> KeyEventSource.MINUS
        DOM_VK_DECIMAL -> KeyEventSource.PERIOD
        DOM_VK_DIVIDE -> KeyEventSource.UNKNOWN
        DOM_VK_F1 -> KeyEventSource.F1
        DOM_VK_F2 -> KeyEventSource.F2
        DOM_VK_F3 -> KeyEventSource.F3
        DOM_VK_F4 -> KeyEventSource.F4
        DOM_VK_F5 -> KeyEventSource.F5
        DOM_VK_F6 -> KeyEventSource.F6
        DOM_VK_F7 -> KeyEventSource.F7
        DOM_VK_F8 -> KeyEventSource.F8
        DOM_VK_F9 -> KeyEventSource.F9
        DOM_VK_F10 -> KeyEventSource.F10
        DOM_VK_F11 -> KeyEventSource.F11
        DOM_VK_F12 -> KeyEventSource.F12
        DOM_VK_F13 -> KeyEventSource.UNKNOWN
        DOM_VK_F14 -> KeyEventSource.UNKNOWN
        DOM_VK_F15 -> KeyEventSource.UNKNOWN
        DOM_VK_F16 -> KeyEventSource.UNKNOWN
        DOM_VK_F17 -> KeyEventSource.UNKNOWN
        DOM_VK_F18 -> KeyEventSource.UNKNOWN
        DOM_VK_F19 -> KeyEventSource.UNKNOWN
        DOM_VK_F20 -> KeyEventSource.UNKNOWN
        DOM_VK_F21 -> KeyEventSource.UNKNOWN
        DOM_VK_F22 -> KeyEventSource.UNKNOWN
        DOM_VK_F23 -> KeyEventSource.UNKNOWN
        DOM_VK_F24 -> KeyEventSource.UNKNOWN
        DOM_VK_NUM_LOCK -> KeyEventSource.NUM_LOCK
        DOM_VK_SCROLL_LOCK -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_FJ_JISHO -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_FJ_MASSHOU -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_FJ_TOUROKU -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_FJ_LOYA -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_FJ_ROYA -> KeyEventSource.UNKNOWN
        DOM_VK_CIRCUMFLEX -> KeyEventSource.UNKNOWN
        DOM_VK_EXCLAMATION -> KeyEventSource.UNKNOWN
        DOM_VK_DOUBLE_QUOTE -> KeyEventSource.UNKNOWN
        DOM_VK_HASH -> KeyEventSource.UNKNOWN
        DOM_VK_DOLLAR -> KeyEventSource.UNKNOWN
        DOM_VK_PERCENT -> KeyEventSource.UNKNOWN
        DOM_VK_AMPERSAND -> KeyEventSource.UNKNOWN
        DOM_VK_UNDERSCORE -> KeyEventSource.UNKNOWN
        DOM_VK_OPEN_PAREN -> KeyEventSource.LEFT_BRACKET
        DOM_VK_CLOSE_PAREN -> KeyEventSource.RIGHT_BRACKET
        DOM_VK_ASTERISK -> KeyEventSource.UNKNOWN
        DOM_VK_PLUS -> KeyEventSource.PLUS
        DOM_VK_PIPE -> KeyEventSource.UNKNOWN
        DOM_VK_HYPHEN_MINUS -> KeyEventSource.MINUS
        DOM_VK_OPEN_CURLY_BRACKET -> KeyEventSource.UNKNOWN
        DOM_VK_CLOSE_CURLY_BRACKET -> KeyEventSource.UNKNOWN
        DOM_VK_TILDE -> KeyEventSource.UNKNOWN
        DOM_VK_VOLUME_MUTE -> KeyEventSource.UNKNOWN
        DOM_VK_VOLUME_DOWN -> KeyEventSource.UNKNOWN
        DOM_VK_VOLUME_UP -> KeyEventSource.UNKNOWN
        DOM_VK_COMMA -> KeyEventSource.COMMA
        DOM_VK_PERIOD -> KeyEventSource.PERIOD
        DOM_VK_SLASH -> KeyEventSource.SLASH
        DOM_VK_BACK_QUOTE -> KeyEventSource.UNKNOWN
        DOM_VK_OPEN_BRACKET -> KeyEventSource.UNKNOWN
        DOM_VK_BACK_SLASH -> KeyEventSource.UNKNOWN
        DOM_VK_CLOSE_BRACKET -> KeyEventSource.UNKNOWN
        DOM_VK_QUOTE -> KeyEventSource.UNKNOWN
        DOM_VK_META -> KeyEventSource.UNKNOWN
        DOM_VK_ALTGR -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_ICO_HELP -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_ICO_00 -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_ICO_CLEAR -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_RESET -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_JUMP -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_PA1 -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_PA2 -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_PA3 -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_WSCTRL -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_CUSEL -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_ATTN -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_FINISH -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_COPY -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_AUTO -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_ENLW -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_BACKTAB -> KeyEventSource.UNKNOWN
        DOM_VK_ATTN -> KeyEventSource.UNKNOWN
        DOM_VK_CRSEL -> KeyEventSource.UNKNOWN
        DOM_VK_EXSEL -> KeyEventSource.UNKNOWN
        DOM_VK_EREOF -> KeyEventSource.UNKNOWN
        DOM_VK_PLAY -> KeyEventSource.UNKNOWN
        DOM_VK_ZOOM -> KeyEventSource.UNKNOWN
        DOM_VK_PA1 -> KeyEventSource.UNKNOWN
        DOM_VK_WIN_OEM_CLEAR -> KeyEventSource.UNKNOWN
        else -> KeyEventSource.UNKNOWN
    }

}
