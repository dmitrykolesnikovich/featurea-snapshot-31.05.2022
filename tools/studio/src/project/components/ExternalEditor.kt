package featurea.studio.project.components

import featurea.runtime.*
import featurea.script.ScriptInterpreter
import featurea.script.executeScript
import featurea.script.firstCanonicalName
import featurea.utils.runOnEditorThread

@OptIn(ExperimentalStdlibApi::class)
fun <T> Component.openExternalEditor(script: String, vararg args: Any? = emptyArray(), onResult: (T) -> Unit = {}) {
    val scriptInterpreter: ScriptInterpreter = import()

    val container = module.container
    val outerModule = container.findOuterModule()
    val firstCanonicalName = scriptInterpreter.firstCanonicalName(script)
    val moduleCanonicalName = "${firstCanonicalName}Module"
    defaultProxyScope {
        initContainer(container)
        injectModule(moduleCanonicalName) {
            include(outerModule)
        }
        complete { externalEditorModule ->
            runOnEditorThread {
                val result: Any = externalEditorModule.executeScript<Any>(script, *args)
                onResult(result as T)
            }
        }
    }
}

/*internals*/

private fun Container.findOuterModule(): Module {
    // project
    val projectModule = findModuleOrNull("featurea.studio.project.ProjectModule")
    if (projectModule != null) return projectModule

    // parent
    val parentContainer = runtime.containerProvider.includes.firstOrNull()
    if (parentContainer != null) {
        val parentModule = parentContainer.modules.firstOrNull()
        if (parentModule != null) return parentModule
    }

    // studio
    val studioModule = findModuleOrNull("featurea.studio.StudioModule")
    if (studioModule != null) return studioModule

    // not found
    error("outer module not found")
}
