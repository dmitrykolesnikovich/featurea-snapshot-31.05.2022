package featurea

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.toSimpleName

private var applicationModule: Module? = null

abstract class ApplicationComponent : Component {
    override val module: Module = checkNotNull(applicationModule)
    val app: Application = import()
}

// just for now todo conceptualize better
suspend fun <T> Component.applicationBlock(block: suspend () -> T): T {
    module.checkApplicationScopeModuleKey()
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
    module.checkApplicationScopeModuleKey()
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
        module.checkApplicationScopeModuleKey()
        applicationModule = module
    }

    fun detachModule(module: Module) {
        check(applicationModule == module)
        applicationModule = null
    }

}

/*internals*/

private val applicationKeyRegexes: List<Regex> = listOf(".*DefaultModule\\d?$".toRegex(), ".*EditorModule\\d?$".toRegex(), ".*ApplicationModule\\d?$".toRegex())

private fun Module.checkApplicationScopeModuleKey() {
    val simpleKey: String = key.toSimpleName()
    for (applicationKeyRegex in applicationKeyRegexes) {
        if (applicationKeyRegex.matches(simpleKey)) {
            return
        }
    }
    error("key: $key")
}
