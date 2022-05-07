package featurea.script

import featurea.utils.Stack

@OptIn(ExperimentalStdlibApi::class)
class ScriptCall(val interpreter: ScriptInterpreter, val imports: Imports, val script: Script, val source: String, val context: ScriptCallContext) {

    constructor(interpreter: ScriptInterpreter, source: String, script: Script, args: Args, imports: Map<String, String>) :
            this(interpreter, imports, script, source, ScriptCallContext(Stack(args.reversed())))

    suspend fun <T : Any?> execute(): T {
        val expressions: List<ScriptExpression> = interpreter.parseSource(source)
        for (index in 0 until expressions.lastIndex) {
            expressions[index].execute(this)
        }
        return expressions.last().execute(this) as T
    }

    suspend fun eval(): String {
        var result: String = source
        val matchResults: Sequence<MatchResult> = expressionRegex.findAll(result)
        for (matchResult in matchResults) {
            val script = matchResult.value.subSequence(2, matchResult.value.lastIndex).toString()
            val executeResult = interpreter.execute<Any>(imports, this.script, script, context)
            result = result.replaceFirst(matchResult.value, executeResult.toString())
        }
        return result
    }

}
