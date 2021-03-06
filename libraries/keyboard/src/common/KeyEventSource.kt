package featurea.keyboard

// this is limited with Latin alphabet todo make more flexible
enum class KeyEventSource(val char: Char? = null) {
    NUM_0('0'),
    NUM_1('1'),
    NUM_2('2'),
    NUM_3('3'),
    NUM_4('4'),
    NUM_5('5'),
    NUM_6('6'),
    NUM_7('7'),
    NUM_8('8'),
    NUM_9('9'),
    A('a'),
    ALT_LEFT,
    ALT_RIGHT,
    APOSTROPHE('\''),
    AT('@'),
    B('b'),
    BACK,
    BACKSLASH('\\'),
    C('c'),
    CALL,
    CAMERA,
    CLEAR,
    COMMA(','),
    D('d'),
    DELETE,
    BACKSPACE,
    FORWARD_DEL,
    DPAD_CENTER,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT,
    DPAD_UP,
    CENTER,
    DOWN,
    LEFT,
    RIGHT,
    UP,
    E('e'),
    ENDCALL,
    ENTER,
    ENVELOPE,
    EQUALS('='),
    EXPLORER,
    F('f'),
    FOCUS,
    G('g'),
    GRAVE,
    H('h'),
    HEADSETHOOK,
    HOME,
    I('i'),
    J('j'),
    K('k'),
    L('l'),
    LEFT_BRACKET('('),
    M('m'),
    MEDIA_FAST_FORWARD,
    MEDIA_NEXT,
    MEDIA_PLAY_PAUSE,
    MEDIA_PREVIOUS,
    MEDIA_REWIND,
    MEDIA_STOP,
    MENU,
    MINUS('-'),
    MUTE,
    N('n'),
    NOTIFICATION,
    NUM,
    O('o'),
    P('p'),
    PERIOD('.'),
    PLUS('+'),
    POUND,
    POWER,
    Q('q'),
    R('r'),
    RIGHT_BRACKET(')'),
    S('s'),
    SEARCH,
    SEMICOLON(';'),
    SHIFT_LEFT,
    SHIFT_RIGHT,
    SLASH('/'),
    SOFT_LEFT,
    SOFT_RIGHT,
    SPACE(' '),
    STAR,
    SYM,
    T('t'),
    TAB,
    U('u'),
    UNKNOWN,
    V('v'),
    VOLUME_DOWN,
    VOLUME_UP,
    W('w'),
    X('x'),
    Y('y'),
    Z('z'),
    META_ALT_LEFT_ON,
    META_ALT_ON,
    META_ALT_RIGHT_ON,
    META_SHIFT_LEFT_ON,
    META_SHIFT_ON,
    META_SHIFT_RIGHT_ON,
    META_SYM_ON,
    CONTROL_LEFT,
    CONTROL_RIGHT,
    ESCAPE,
    END,
    INSERT,
    PAGE_UP,
    PAGE_DOWN,
    PICTSYMBOLS,
    SWITCH_CHARSET,
    BUTTON_CIRCLE,
    BUTTON_A,
    BUTTON_B,
    BUTTON_C,
    BUTTON_X,
    BUTTON_Y,
    BUTTON_Z,
    BUTTON_L1,
    BUTTON_R1,
    BUTTON_L2,
    BUTTON_R2,
    BUTTON_THUMBL,
    BUTTON_THUMBR,
    BUTTON_START,
    BUTTON_SELECT,
    BUTTON_MODE,
    NUMPAD_0('0'),
    NUMPAD_1('1'),
    NUMPAD_2('2'),
    NUMPAD_3('3'),
    NUMPAD_4('4'),
    NUMPAD_5('5'),
    NUMPAD_6('6'),
    NUMPAD_7('7'),
    NUMPAD_8('8'),
    NUMPAD_9('9'),
    COLON(':'),
    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,
    TOUCH,
    MOUSE_RIGHT,
    MOUSE_WHEEL,
    CAPS_LOCK,
    NUM_LOCK
}


fun Char?.toKeyEventSource(): KeyEventSource {
    val values = enumValues<KeyEventSource>()
    for (value in values) {
        if (value.char == this) {
            return value
        }
    }
    return KeyEventSource.UNKNOWN
}
