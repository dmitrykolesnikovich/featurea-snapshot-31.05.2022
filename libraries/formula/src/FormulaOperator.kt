package featurea.formula

sealed class FormulaOperator<L, R, T> : FormulaOperand<T> {

    lateinit var leftOperand: FormulaOperand<L>
    lateinit var rightOperand: FormulaOperand<R>

    abstract fun function(leftValue: L, rightValue: R): T

    override fun calculate(formula: Formula<*>): T {
        val leftHandValue = leftOperand.calculate(formula)
        val rightHandValue = rightOperand.calculate(formula)
        return function(leftHandValue, rightHandValue)
    }

}

class Plus : FormulaOperator<Double, Double, Double>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue + rightValue
}

class Minus : FormulaOperator<Double, Double, Double>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue - rightValue
}

class Multiply : FormulaOperator<Double, Double, Double>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue * rightValue
}

class Divide : FormulaOperator<Double, Double, Double>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue / rightValue
}

class Equal : FormulaOperator<Any, Any, Boolean>() {
    override fun function(leftValue: Any, rightValue: Any): Boolean {
        if (leftValue is Double && rightValue is Double) {
            return leftValue == rightValue
        } else if (leftValue is Boolean && rightValue is Boolean) {
            return leftValue == rightValue
        } else {
            throw IllegalArgumentException("leftValue: $leftValue, rightValue: $rightValue")
        }
    }
}

class Greater : FormulaOperator<Double, Double, Boolean>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue > rightValue
}

class GreaterOrEqual : FormulaOperator<Double, Double, Boolean>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue >= rightValue
}

class Less : FormulaOperator<Double, Double, Boolean>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue < rightValue
}

class LessOrEqual : FormulaOperator<Double, Double, Boolean>() {
    override fun function(leftValue: Double, rightValue: Double) = leftValue <= rightValue
}
