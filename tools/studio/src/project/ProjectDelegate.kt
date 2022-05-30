package featurea.studio.project

import featurea.content.ResourceTag
import featurea.runtime.Component
import featurea.runtime.ModuleBlock

interface ProjectDelegate : Component {
    fun newDocument(name: String): ResourceTag = error("unsupported")
    fun openDocument(documentId: String, complete: ModuleBlock)
}
