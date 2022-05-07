package featurea.script

import featurea.utils.Stack
import featurea.utils.splitWithWrappers

@ExperimentalStdlibApi
class ScriptExpression(interpreter: ScriptInterpreter, value: String) {

    private val leftHandOperators = Stack<ScriptLeftOperator>()
    private val rightHandOperator: ScriptRightOperator

    init {
        val tokens = value.splitWithWrappers('=')
        for (index in 0 until tokens.size - 1) {
            val leftValue = tokens[index]
            leftHandOperators.push(ScriptLeftOperator(interpreter, leftValue))
        }
        val rightValue = tokens[tokens.lastIndex]
        rightHandOperator = ScriptRightOperator(interpreter, rightValue)
    }

    suspend fun execute(scriptCall: ScriptCall): Any? {
        val rightValue = rightHandOperator.execute(scriptCall)
        scriptCall.context.result = rightValue
        var leftHandValue = rightValue
        while (leftHandOperators.isNotEmpty()) {
            leftHandValue = leftHandOperators.pop().execute(scriptCall)
        }
        return leftHandValue
    }

}
