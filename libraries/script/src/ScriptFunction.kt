package featurea.script

import featurea.toMap

class ScriptFunction(private val interpreter: ScriptInterpreter, private val value: String, private val argNames: List<String>) {

    suspend fun execute(args: List<Any>, script: Script): Any? {
        val context: ScriptCallContext = ScriptCallContext(argNames toMap args)
        return interpreter.execute(script = script, source = value, context = context)
    }

}
