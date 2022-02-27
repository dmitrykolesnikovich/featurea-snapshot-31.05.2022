package featurea

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

private var applicationModule: Module? = null

abstract class ApplicationComponent : Component {
    override val module: Module = checkNotNull(applicationModule)
    val app: Application = import()
}

// just for now todo conceptualize better
suspend fun <T> Component.applicationBlock(block: suspend () -> T): T {
    module.checkApplicationKey()
    val isEnter: Boolean = applicationModule == null
    if (isEnter) {
        applicationModule = module
    } else {
        check(applicationModule == module)
    }
    val result: T = block()
    if (isEnter) {
        applicationModule = null
    }
    return result
}

fun <T> Component.applicationScope(block: () -> T): T {
    module.checkApplicationKey()
    val isEntering: Boolean = applicationModule == null
    if (isEntering) {
        applicationModule = module
    } else {
        check(applicationModule == module)
    }
    val result: T = block()
    if (isEntering) {
        applicationModule = null
    }
    return result
}

object ApplicationScope {

    fun attachModule(module: Module) {
        check(applicationModule == null)
        module.checkApplicationKey()
        applicationModule = module
    }

    fun detachModule(module: Module) {
        check(applicationModule == module)
        applicationModule = null
    }

}

/*internals*/

private fun Module.checkApplicationKey() {
    val simpleKey: String = key.toSimpleName()
    check(simpleKey.startsWithAny("DefaultModule", "WindowModule"))
}
