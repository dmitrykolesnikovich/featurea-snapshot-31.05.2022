@file:Suppress("UNUSED_VARIABLE", "RedundantExplicitType")

package featurea.utils

import featurea.jvm.methodOrNull
import featurea.runtime.*
import java.lang.reflect.Method

// Hot Reload is ability to reload application without starting new JVM instance

private var reloadApplicationFunction: String? = null

fun configureApplicationReloadFunction(function: String) {
    reloadApplicationFunction = function
}

// IMPORTANT should be prepended with `container.clearCaches()`
fun Container.reloadExistingApplicationModule() {
    val appModule: Module = modules["featurea.app.ApplicationModule"]
    reloadExistingModule(appModule)
}

fun Container.reloadExistingModule(existingModule: Module) {
    val reloadApplicationFunction: String = checkNotNull(reloadApplicationFunction)
    val container: Container = this
    val lastIndexOfDot: Int = reloadApplicationFunction.lastIndexOf(".")
    val (javaCanonicalName, function) = reloadApplicationFunction.divide(lastIndexOfDot)
    val packageId: String = reloadApplicationFunction.packageId
    val simpleName: String = reloadApplicationFunction.toSimpleName()
    val simpleNameKt: String = simpleName.toKotlinClassName()
    val canonicalClassNameKt: String = "${packageId}.${simpleNameKt}"
    val javaClass: Class<*> = Class.forName(canonicalClassNameKt)
    val javaMethod: Method? = javaClass.methodOrNull(function)
    if (javaMethod != null) {
        val newRuntime: Runtime = javaMethod.invoke(null) as Runtime
        val newModule: Module = Module(newRuntime, container)
        val existingModuleKey: String = existingModule.key
        container.removeModule(existingModule)
        container.injectModule(existingModuleKey, newModule)
        log("existingModule: $existingModule")
        log("newModule: $newModule")
        // todo launch newModule properly
    }
}
