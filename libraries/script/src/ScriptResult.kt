package featurea.script

import featurea.utils.isDouble
import featurea.utils.isInteger
import featurea.utils.isWrapped
import featurea.script.ScriptResult.Absent
import featurea.script.ScriptResult.Existing

internal sealed class ScriptResult {
    object Absent : ScriptResult()
    class Existing(val value: Any? = Unit) : ScriptResult()
}

internal fun scriptResultOf(primitiveValue: String): ScriptResult = when {
    primitiveValue == "null" -> Existing(null)
    primitiveValue == "false" -> Existing(false)
    primitiveValue == "true" -> Existing(true)
    primitiveValue.isWrapped("'") -> Existing(primitiveValue.substring(1, primitiveValue.lastIndex))
    primitiveValue.isInteger() -> Existing(primitiveValue.toInt())
    primitiveValue.isDouble() -> Existing(primitiveValue.toDouble())
    else -> Absent
}

