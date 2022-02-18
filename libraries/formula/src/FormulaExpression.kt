package featurea.formula

@Suppress("UNCHECKED_CAST")
class FormulaExpression(val value: String) {

    private val operators = ArrayList<FormulaOperator<Any, Any, Any>>()
    lateinit var operand: FormulaOperand<Any>

    init {
        parseOperators()
        orderOperators()
    }

    private fun parseOperators() {
        val chars: CharArray = value.toCharArray()
        var operatorToken: String = ""
        var currentOperator: FormulaOperator<Any, Any, Any>? = null
        var operandToken: String = ""
        var currentOperand: FormulaOperand<Any>? = null
        var index: Int = 0
        while (index < chars.size) {
            val ch = chars[index]
            if (ch == ' ') {
                index++
                continue
            }
            if (ch.isOperatorSymbol()) {
                // 1. operand
                if (!operandToken.isEmpty()) {
                    currentOperand = operandToken.toOperand()
                    if (currentOperator != null) {
                        currentOperator.rightOperand = currentOperand
                    }
                    operandToken = ""
                }
                // 2. operator
                operatorToken += ch
            } else {
                // 1. operator
                if (!operatorToken.isEmpty()) {
                    currentOperator = operatorMap[operatorToken]?.invoke() as FormulaOperator<Any, Any, Any>
                    operators.add(currentOperator)
                    currentOperator.leftOperand = currentOperand ?: error("value:\n$value")
                    operatorToken = ""
                }
                if (ch == '(') {
                    index++
                    var exprIndex: Int = index
                    var counter: Int = 1
                    while (counter != 0) {
                        if (value[exprIndex] == ')') {
                            counter--
                        } else if (value[exprIndex] == '(') {
                            counter++
                        }
                        exprIndex++
                    }
                    exprIndex--
                    val expression: FormulaExpression = FormulaExpression(value.substring(index, exprIndex))
                    index = exprIndex
                    val expressionOperand = expression.operand
                    if (currentOperator != null) {
                        currentOperator.rightOperand = expressionOperand
                    }
                    currentOperand = expressionOperand
                } else {
                    // 2. operand
                    operandToken += ch
                    if (index == chars.size - 1) {
                        currentOperand = operandToken.toOperand()
                        if (currentOperator != null) {
                            currentOperator.rightOperand = currentOperand
                        }
                    }
                }
            }
            index++
        }
        if (operators.isEmpty()) {
            operand = currentOperand as FormulaOperand<Any>
        }
    }

    private fun orderOperators() {
        if (operators.isEmpty()) {
            return
        }
        operators.sortOperators()
        val markers: Markers = mutableMapOf<FormulaOperand<Any>, FormulaOperator<Any, Any, Any>>()
        for (operator in operators) {
            run /*1. left operand*/{
                val leftOperand: FormulaOperand<Any> = operator.leftOperand
                val marker: FormulaOperator<Any, Any, Any>? = markers[leftOperand]
                if (marker != null) {
                    operator.leftOperand = marker
                    markers.updateMarkers(marker, operator)
                } else {
                    markers[leftOperand] = operator
                }
            }
            run /*2. right operand*/{
                val rightOperand: FormulaOperand<Any> = operator.rightOperand
                val marker: FormulaOperator<Any, Any, Any>? = markers[rightOperand]
                if (marker != null) {
                    operator.rightOperand = marker
                    markers.updateMarkers(marker, operator)
                } else {
                    markers[rightOperand] = operator
                }
            }
        }
        operand = operators.last()
    }

}

/*internals*/

private fun Markers.updateMarkers(from: FormulaOperator<Any, Any, Any>, to: FormulaOperator<Any, Any, Any>) {
    val markers: Markers = this
    for ((operand, operator) in this) {
        if (operator == from) {
            markers[operand] = to
        }
    }
}

private typealias Markers = MutableMap<FormulaOperand<Any>, FormulaOperator<Any, Any, Any>>
