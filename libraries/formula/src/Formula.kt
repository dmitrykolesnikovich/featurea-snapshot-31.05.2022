package featurea.formula

@Suppress("UNCHECKED_CAST")
class Formula<T>(val value: String) {

    val variables: Variables = Variables()

    private val operand: FormulaOperand<T> = FormulaExpression(value).operand as FormulaOperand<T>

    fun calculate(): T {
        val formula: Formula<T> = this
        return operand.calculate(formula)
    }

    fun variables(block: Formula<T>.() -> Unit): Formula<T> {
        block()
        return this
    }

    infix fun String.to(value: Any) {
        variables[this] = value
    }

    fun setVariable(name: String, value: Any) {
        variables[name] = when (value) {
            is Number -> value.toDouble()
            is Boolean -> value
            else -> error("value: $value")
        }
    }

    inner class Variables {

        private val map = mutableMapOf<String, Any>()

        operator fun set(name: String, value: Any) {
            map[name] = when (value) {
                is Number -> value.toDouble()
                is Boolean -> value
                else -> throw IllegalArgumentException("value: $value")
            }
        }

        operator fun get(name: String): Any? {
            return map[name]
        }

    }

}

fun <T> String.toFormulaOrNull(): Formula<T>? = if (isNotBlank()) Formula(this) else null
