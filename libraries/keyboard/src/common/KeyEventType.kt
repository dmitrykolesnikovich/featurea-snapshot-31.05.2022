package featurea.keyboard

import featurea.utils.Specified

// so for now for all character keys I use RELEASE event type
enum class KeyEventType(override val specifier: String) : Specified {
    PRESS("press"),     // FIXME: PRESS not working for character keys on mobile browsers
    RELEASE("release"),
    CLICK("click")      // FIXME: CLICK not working for character keys on mobile browsers
}
