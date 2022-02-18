package featurea.script

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.script") {
    "ScriptInterpreter" to ScriptInterpreter::class

    static {
        provideComponent(ScriptInterpreter())
    }
}

/*types*/

typealias Args = List<Any?>

typealias Imports = Map<String, String>

fun args(): Args = emptyList()

fun imports(): Imports = emptyMap()
