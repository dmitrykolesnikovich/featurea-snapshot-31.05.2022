package featurea.modbus.support

import featurea.Specified
import featurea.Specifier

enum class ScriptType(override val specifier: Specifier) : Specified {
    Timeout("Timeout"),
    WriteEvent("WriteEvent")
}
