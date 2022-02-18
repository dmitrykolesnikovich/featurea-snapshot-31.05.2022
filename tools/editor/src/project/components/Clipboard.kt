package featurea.studio.project.components

import featurea.desktop.jfx.isEditing
import featurea.utils.runOnEditorThread
import featurea.content.ResourceTag
import featurea.rml.deepCopy
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.project.Project

class Clipboard(override val module: Module) : Component {

    private val project: Project = import()

    private var rmlTags: List<ResourceTag>? = null

    fun cut() {
        val editor: Editor = findFocusedEditor() ?: return
        val rmlTags = editor.selectionService.selectedRmlTags
        editor.remove(*rmlTags.toTypedArray())
        this.rmlTags = rmlTags
    }

    fun copy() {
        val editor: Editor = findFocusedEditor() ?: return
        this.rmlTags = editor.selectionService.selectedRmlTags
    }

    fun paste() {
        val rmlTags: List<ResourceTag> = rmlTags ?: return
        val editor: Editor = findFocusedEditor() ?: return
        val copiedRmlTags = rmlTags.map { it.deepCopy(parent = null) }
        editor.append(*copiedRmlTags.toTypedArray())
        editor.updateEditorUi {
            runOnEditorThread {
                editor.selectionService.select(*copiedRmlTags.toTypedArray())
            }
        }
    }

    /*internals*/

    private fun findFocusedEditor(): Editor? {
        val editor = project.findSelectedEditorOrNull() ?: return null
        if (!editor.mainStage.isFocused) return null
        if (editor.rmlTableView.isEditing()) return null
        return editor
    }

}
