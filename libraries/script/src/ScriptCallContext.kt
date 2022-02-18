package featurea.script

import featurea.utils.Stack

class ScriptCallContext {

    val args: Stack<Any?>
    val localVariables: MutableMap<String, Any?>
    var result: Any? = null

    constructor(args: Stack<Any?>) {
        this.args = args
        this.localVariables = mutableMapOf()
    }

    constructor(localVariables: MutableMap<String, Any?>) {
        this.args = Stack()
        this.localVariables = localVariables
    }

}

/*convenience*/

val ScriptCallContext.hasArguments: Boolean get() = args.isNotEmpty()

val ScriptCallContext.hasLocalVariables: Boolean get() = localVariables.isNotEmpty()

val ScriptCallContext.hasResult: Boolean get() = result != null
