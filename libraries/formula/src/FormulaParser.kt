package featurea.formula

private const val operatorSymbols = "+-*/<=>"

fun Char.isOperatorSymbol() = operatorSymbols.contains(this)

val operatorMap = mapOf<String, () -> FormulaOperator<*, *, *>>(
    "+" to { Plus() },
    "-" to { Minus() },
    "*" to { Multiply() },
    "/" to { Divide() },
    "==" to { Equal() },
    ">" to { Greater() },
    ">=" to { GreaterOrEqual() },
    "<" to { Less() },
    "<=" to { LessOrEqual() }
)

private val operatorPriorityComparator = Comparator<FormulaOperator<*, *, *>> { operator1, operator2 ->
    when {
        (operator1::class == operator2::class) -> 0
        operator1.isBoolean -> 1
        operator1.isNonLinear -> -1
        operator1.isLinear -> if (operator2.isBoolean) -1 else 1
        else -> error("$operator1, $operator2")
    }
}

fun ArrayList<FormulaOperator<Any, Any, Any>>.sortOperators() {
    val sortedOperators = sortedWith(operatorPriorityComparator)
    clear()
    addAll(sortedOperators)
}

private val FormulaOperator<*, *, *>.isBoolean
    get() = this is Equal || this is Less || this is LessOrEqual || this is Greater || this is GreaterOrEqual

private val FormulaOperator<*, *, *>.isLinear
    get() = this is Plus || this is Minus

private val FormulaOperator<*, *, *>.isNonLinear
    get() = this is Multiply || this is Divide
