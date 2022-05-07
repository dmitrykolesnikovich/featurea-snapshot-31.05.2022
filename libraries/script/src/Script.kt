package featurea.script

interface Script {
    suspend fun execute(action: String, args: List<Any?> = emptyList(), isSuper: Boolean = false): Any?
}

suspend fun Script.findScript(key: String): Script = findScriptOrNull(key) ?: error("key: $key")

suspend fun Script.findScriptOrNull(key: String): Script? {
    val result: Any? = execute(key)
    return result as Script?
}
