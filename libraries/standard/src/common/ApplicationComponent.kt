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

suspend fun <T> Component.applicationScopeBlocking(block: suspend () -> T): T {
    ApplicationScope.checkModule(module)
    val entering: Boolean = applicationModule == null
    if (entering) {
        applicationModule = module
    } else {
        check(applicationModule == module)
    }
    val result: T = block()
    if (entering) {
        applicationModule = null
    }
    return result
}

fun <T> Component.applicationScope(block: () -> T): T {
    ApplicationScope.checkModule(module)
    val entering: Boolean = applicationModule == null
    if (entering) {
        applicationModule = module
    } else {
        check(applicationModule == module)
    }
    val result: T = block()
    if (entering) {
        applicationModule = null
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
        check(applicationModule == null)
        applicationModule = module
    }

    fun detachModule(module: Module) {
        checkModule(module)
        check(applicationModule == module)
        applicationModule = null
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
