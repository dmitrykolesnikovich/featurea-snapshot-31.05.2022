package featurea

import featurea.jvm.methodOrNull
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.Runtime
import java.lang.reflect.Method

// hot reload is ability to reload application without starting new JVM instance

private var reloadFunction: String? = null

// used by engine/gradle/featurea/examples.gradle:124
fun configureHotReload(function: String) {
    reloadFunction = function
}

fun Container.reload(existingModule: Module) {
    val reloadFunction: String = checkNotNull(reloadFunction)
    val container: Container = this
    val lastIndexOfDot: Int = reloadFunction.lastIndexOf(".")
    val (javaCanonicalName, function) = reloadFunction.divide(lastIndexOfDot)
    val packageId: String = reloadFunction.packageId
    val simpleName: String = reloadFunction.toSimpleName()
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
