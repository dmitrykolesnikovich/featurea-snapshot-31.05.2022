@file:Suppress("UNCHECKED_CAST")

package featurea.runtime

typealias Action = Component.() -> Unit

typealias ComponentConstructor<T> = (Module) -> T

typealias Task = suspend Component.() -> Unit

interface Component {
    val module: Module
    fun onCreateComponent() {}
    fun onDeleteComponent() {}
}

// constructor
fun DefaultComponent(module: Module): Component = object : Component {
    override val module: Module = module
}

/*convenience*/

interface ComponentListener {
    fun provideComponent(canonicalName: String, component: Any) {}
}

inline fun <reified T : Any> Component.import(): T {
    module.components.pullTransaction(component = this)
    return module.importComponent()
}

fun <T : Any> Component.import(delegate: Delegate<in T>): T {
    return module.importComponent(delegate as Delegate<T>)
}

inline fun <reified T : Any> Component.create(noinline init: T.() -> Unit = {}): T {
    module.components.pullTransaction(component = this)
    return module.createComponent(init)
}

fun Component.delete() {
    onDeleteComponent()
}

fun Component.executeAction(actionId: String) {
    val action: Action = module.importComponent(actionId)
    action()
}

val Component.container: Container get() = module.container
