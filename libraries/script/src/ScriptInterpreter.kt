package featurea.script

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.splitAndTrim

class ScriptInterpreter {

    val functions = mutableMapOf<String, ScriptFunction>()

    suspend fun <T : Any?> execute(
        imports: Imports = imports(),
        script: Script,
        source: String,
        args: Args = args()
    ): T {
        return ScriptCall(this, source, script, args, imports).execute()
    }

    suspend fun <T : Any?> execute(
        imports: Imports = imports(),
        module: Module,
        source: String,
        context: ScriptCallContext
    ): T {
        return execute(imports, module.createScript(), source, context)
    }

    suspend fun <T : Any?> execute(
        imports: Imports = imports(),
        script: Script,
        source: String,
        context: ScriptCallContext
    ): T {
        return ScriptCall(this, imports, script, source, context).execute()
    }

    suspend fun eval(code: String, script: Script, args: Args = args(), imports: Imports = imports()): String {
        return ScriptCall(this, code, script, args, imports).eval()
    }

}

suspend fun <T : Any?> Component.executeScript(script: String, vararg args: Any? = emptyArray()): T {
    return module.executeScript(script, *args)
}

suspend fun <T : Any?> Module.executeScript(script: String, vararg args: Any? = emptyArray()): T {
    val scriptInterpreter: ScriptInterpreter = importComponent()
    return scriptInterpreter.execute(imports(), createScript(), script, listOf(*args))
}

@ExperimentalStdlibApi
fun ScriptInterpreter.firstCanonicalName(script: String): String {
    val firstLine: String = script.splitAndTrim(";").first()
    return ScriptRightOperator(this, firstLine).key
}

/*internals*/

private fun Module.createScript(): Script = object : Script {
    override suspend fun executeAction(action: String, args: Args, isSuper: Boolean): ScriptResult {
        return importComponent(action)
    }
}
