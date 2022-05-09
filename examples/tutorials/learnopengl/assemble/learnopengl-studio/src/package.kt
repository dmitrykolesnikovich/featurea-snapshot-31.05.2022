package featurea.examples.learnopengl.studio

import featurea.utils.featureaDir
import featurea.runtime.*
import featurea.studio.editor.DefaultEditorDelegate
import featurea.studio.home.StudioDelegate
import featurea.studio.project.*
import featurea.studio.project.menu.SaveMenu
import java.io.File

/*dependencies*/

val artifact = Artifact("learnopengl.studio") {
    includeContentRootWithConfig { "$featureaDir/projects/whoplee/learnopengl/plugins/learnopengl-studio/res" }
    include(featurea.studio.artifact)
    include(learnopengl.artifact)

    "LearnopenglStudioDelegate" to ::LearnopenglStudioDelegate
    "LearnopenglProjectDelegate" to ::LearnopenglProjectDelegate

    ProjectPlugin {
        "project.SaveMenu" to ::SaveMenu
        "project.OpenFormMenu" to ::OpenTestMenu
    }
}

/*studio*/

class LearnopenglProjectDelegate(override val module: Module) : ProjectDelegate {
    private val project: Project = import()
    override fun openDocument(documentId: String, complete: ModuleBlock) {
        project.openEditor<DefaultEditorDelegate>(documentId)
    }
}

class LearnopenglStudioDelegate(override val module: Module) : StudioDelegate {
    override fun openProject(file: File) = launch {
        ProjectRuntime(studioModule = module) {
            delegate = import<LearnopenglProjectDelegate>()
            rmlFile = file
        }
    }
}
