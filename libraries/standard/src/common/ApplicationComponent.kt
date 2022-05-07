package featurea

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.toSimpleName

private var applicationScopeModule: Module? = null

abstract class ApplicationComponent : Component {
    override val module: Module = checkNotNull(applicationScopeModule)
}

// just for now todo conceptualize better
suspend fun <T> Component.applicationScopeBlocking(block: suspend () -> T): T {
    ApplicationScope.checkModule(module)
    val entering: Boolean = applicationScopeModule == null
    if (entering) {
        applicationScopeModule = module
    } else {
        check(applicationScopeModule == module)
    }
    val result: T = block()
    if (entering) {
        applicationScopeModule = null
    }
    return result
}

fun <T> Component.applicationScope(block: () -> T): T {
    ApplicationScope.checkModule(module)
    val entering: Boolean = applicationScopeModule == null
    if (entering) {
        applicationScopeModule = module
    } else {
        check(applicationScopeModule == module)
    }
    val result: T = block()
    if (entering) {
        applicationScopeModule = null
    }
    return result
}

object ApplicationScope {

    private val moduleKeyRegexes: List<Regex> = listOf(
        ".*ApplicationModule\\d?$".toRegex(),
        ".*DefaultModule\\d?$".toRegex(),
        ".*EditorModule\\d?$".toRegex(),
    )

    fun attachModule(module: Module) {
        checkModule(module)
        check(applicationScopeModule == null)
        applicationScopeModule = module
    }

    fun detachModule(module: Module) {
        checkModule(module)
        check(applicationScopeModule == module)
        applicationScopeModule = null
    }

    fun checkModule(module: Module) {
        val key: String = module.key
        val simpleKey: String = key.toSimpleName()
        for (applicationKeyRegex in moduleKeyRegexes) {
            if (applicationKeyRegex.matches(simpleKey)) {
                return
            }
        }
        error("key: $key")
    }

}
