package featurea.script

import featurea.utils.Scope

interface Script {
    suspend fun execute(action: String, args: List<Any?> = emptyList(), scope: Scope = Scope.Inner): Any?
}

suspend fun Script.findScript(key: String): Script = findScriptOrNull(key) ?: error("key: $key")

suspend fun Script.findScriptOrNull(key: String): Script? {
    val result: Any? = execute(key)
    return result as Script?
}
