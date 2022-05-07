package featurea.modbus.support

import featurea.utils.Specified
import featurea.utils.Specifier

enum class ScriptType(override val specifier: Specifier) : Specified {
    Timeout("Timeout"),
    WriteEvent("WriteEvent")
}
