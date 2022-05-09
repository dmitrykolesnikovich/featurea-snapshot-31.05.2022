@file:Suppress("UNCHECKED_CAST", "LiftReturnOrAssignment")

package featurea.formula

import featurea.utils.isBoolean
import featurea.utils.isDouble
import featurea.utils.isInteger

interface FormulaOperand<T> {
    fun calculate(formula: Formula<*>): T
}

class ValueOperand<T>(val value: T) : FormulaOperand<T> {
    override fun calculate(formula: Formula<*>) = value
}

class VariableOperand<T>(val name: String) : FormulaOperand<T> {
    override fun calculate(formula: Formula<*>): T = formula.variables[name] as T
}

fun <T> String.toOperand(): FormulaOperand<T> {
    if (isDouble() || isInteger()) {
        return ValueOperand(toDouble() as T)
    } else if (isBoolean()) {
        return ValueOperand(toBoolean() as T)
    } else {
        return VariableOperand(this)
    }
}
