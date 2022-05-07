package featurea.script

import featurea.utils.divide
import featurea.formula.Formula
import featurea.utils.packageId
import featurea.utils.splitWithWrappers
import featurea.utils.toSimpleName

sealed class ScriptOperator(val interpreter: ScriptInterpreter, val value: String) {
    abstract suspend fun execute(scriptCall: ScriptCall): Any?
}

@ExperimentalStdlibApi
class ScriptRightOperator(interpreter: ScriptInterpreter, value: String) : ScriptOperator(interpreter, value) {

    val key: String
    private val action: String?
    private val argsString: String?

    init {
        val actionId: String
        val firstIndexOfOpenBrace = value.indexOf('(')
        val lastIndexOfCloseBrace = value.lastIndexOf(')')
        if (firstIndexOfOpenBrace != -1 && lastIndexOfCloseBrace == value.lastIndex) {
            actionId = value.substring(0, firstIndexOfOpenBrace)
            this.argsString = value.substring(firstIndexOfOpenBrace + 1, value.lastIndex)
        } else {
            actionId = value
            this.argsString = null
        }
        // >> quickfix todo improve
        val lastDotIndex = actionId.lastIndexOf(".")
        if (lastDotIndex != -1) {
            this.key = actionId.substring(0, lastDotIndex)
            this.action = actionId.substring(lastDotIndex + 1)
        } else {
            this.key = value
            this.action = null
        }
        // <<
    }

    override suspend fun execute(scriptCall: ScriptCall): Any? {
        val context: ScriptCallContext = scriptCall.context
        val argsList = evaluateArguments(argsString, scriptCall)
        run /*1. action*/ {
            if (action != null && argsString != null) {
                val importKey: String = scriptCall.imports[key] ?: key
                val importedScript: Script? = context.localVariables[importKey] as Script?
                val packageId: String = importKey.packageId
                val simpleName: String = importKey.toSimpleName()
                val docketName: String = "${packageId}.Docket"
                val script: Script? = importedScript ?: scriptCall.script.findScriptOrNull(docketName)
                if (script != null) {
                    val docketAction: String = "${simpleName}.${action}"
                    return script.execute(docketAction, argsList)
                }
            }
        }
        run /*2. local variable from stack*/ {
            val localVariable: Any? = context.localVariables[value]
            if (localVariable != null) {
                return localVariable
            }
        }
        run /*3. primitive value*/ {
            val result: ScriptResult = scriptResultOf(value)
            if (result is ScriptResult.Existing) {
                return result.value
            }
        }
        run /*4. argument*/ {
            if (context.hasArguments) {
                return context.args.pop()
            }
        }
        /*5. execute config*/
        /*run  {
            if (key.endsWith("Config")) {
                val configName = key.removeSuffix("Config")
                val configValue = interpreter.contentCache.configCache.getValue("$configName:$action")
                if (configValue != null) {
                    val configImports = interpreter.contentCache.scriptCache.importsOf(configName)
                    return interpreter.execute(configImports, scriptRoutine.script, configValue, context)
                }
            }
        }*/
        run /*6. do math*/{
            return evaluateMath(value, scriptCall)
        }
    }

    /*internals*/

    private suspend fun evaluateArguments(argsString: String?, scriptCall: ScriptCall): Args {
        if (argsString == null) {
            return emptyList()
        }
        val argsList = argsString.splitWithWrappers(',')
        return argsList.map { ScriptRightOperator(interpreter, it).execute(scriptCall) }
    }

    private fun <T> evaluateMath(math: String, scriptCall: ScriptCall): T {
        var value: String = math
        for ((localVariableName, localVariable) in scriptCall.context.localVariables) {
            value = value.replace(localVariableName, localVariable.toString())
        }
        val formula = Formula<T>(value)
        return formula.calculate()
    }

}

class ScriptLeftOperator(interpreter: ScriptInterpreter, value: String) : ScriptOperator(interpreter, value) {

    override suspend fun execute(scriptCall: ScriptCall): Any? {
        val context: ScriptCallContext = scriptCall.context
        val result: Any? = context.result
        if (value.contains("var ")) {
            // 1. create new local variable in stack
            val varName = value.removePrefix("var ")
            varName.validateVarName()
            context.localVariables[varName] = result
        } else if (context.localVariables.containsKey(value)) {
            // 2. existing local variable
            context.localVariables[value] = result
        } else {
            // 3. setter action
            val (key, action) = value.divide(".")
            if (action == null) {
                throw IllegalArgumentException("value: $value")
            }
            val importKey: String = scriptCall.imports[key] ?: key
            val script: Script = scriptCall.script.findScript(importKey)
            script.execute(action, args = listOf(result))
        }
        return result
    }

}

/*internals*/

private fun String.validateVarName() {
    if (contains(".")) throw ScriptSyntaxException("Illegal var name because contains dot symbol: $this")
}

private fun String.isImportDirective(): Boolean = startsWith("import ") && contains(" as ")
