package featurea.studio.editor

import featurea.runtime.Module
import featurea.runtime.ModuleBlock
import featurea.runtime.Runtime

fun EditorRuntime(projectModule: Module, setup: ModuleBlock) = Runtime {
    initContainer(projectModule.container)
    appendModule("featurea.studio.editor.EditorModule") {
        include(projectModule)
    }
    complete(setup)
}

fun DefaultEditorRuntime(projectModule: Module, setup: ModuleBlock) = Runtime {
    initContainer(projectModule.container)
    appendDefaultModule {
        include(projectModule)
    }
    complete(setup)
}
