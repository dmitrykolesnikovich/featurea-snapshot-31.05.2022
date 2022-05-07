package featurea.script

import featurea.utils.splitAndTrim
import featurea.splitWithWrappers

class ScriptSyntaxException(message: String) : RuntimeException(message)

const val DOLLAR = "$"
private val includeRegex = "(include .*?\\.*?\\;)".toRegex() // IntelliJ consider '\\' redundant but it's not
private val functionRegex = "(function .*?\\{.*?\\})".toRegex() // IntelliJ consider '\\' redundant but it's not
internal val expressionRegex = "\\$\\{(.*?)\\}".toRegex() // IntelliJ consider '\\' redundant but it's not

@OptIn(ExperimentalStdlibApi::class)
suspend fun ScriptInterpreter.parseSource(source: String): List<ScriptExpression> {
    var scriptLine = source
    val expressions = ArrayList<ScriptExpression>()
    /*
    run {
        // 1. includes
        for (matchResult in includeRegex.findAll(scriptLine)) {
            val includeDirective = matchResult.value
            addIncludeDirective(includeDirective)
            scriptLine = scriptLine.replaceFirst(includeDirective, "")
        }
    }
    */
    run {
        // 2. actions
        for (matchResult in functionRegex.findAll(scriptLine)) {
            val function = matchResult.value
            addFunction(function)
            scriptLine = scriptLine.replaceFirst(function, "")
        }
    }
    run {
        // 3. expressions
        val tokens = scriptLine.splitWithWrappers(';')
        for (token in tokens) {
            expressions.add(ScriptExpression(this, token))
        }
    }
    return expressions
}

/*internals*/

/*
private suspend fun ScriptInterpreter.addIncludeDirective(includeDirective: String) {
    val scriptFilePath = includeDirective.trim().replaceFirst("include ", "").replaceFirst(";", "").withExtension("fs")
    val scriptFileText = textContent.getText(scriptFilePath)
    if (scriptFileText != null) {
        parseScript(scriptFileText)
    }
}
*/

private fun ScriptInterpreter.addFunction(actionDirective: String) {
    val functionSignature = actionDirective.trim().replaceFirst("function ", "")
    val indexOfOpenBracket = functionSignature.indexOf('(')
    val indexOfCloseBracket = functionSignature.indexOf(')')
    val indexOfOpenCurlyBracket = functionSignature.indexOf('{')
    val functionId = functionSignature.substring(0, indexOfOpenBracket) // action id has single token by design
    val argsList = functionSignature.substring(indexOfOpenBracket, indexOfCloseBracket).splitAndTrim(",")
    val script = functionSignature.substring(indexOfOpenCurlyBracket, functionSignature.length - 1)
    functions[functionId] = ScriptFunction(this, script, argsList)
}
