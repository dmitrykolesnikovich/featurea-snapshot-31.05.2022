package featurea.studio.editor

import featurea.content.ResourceAttribute
import featurea.content.ResourceTag
import featurea.desktop.MainStageProxy
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.*
import featurea.studio.project.Project
import featurea.utils.Property
import featurea.window.Window
import featurea.window.notifyResize
import javafx.stage.Stage

class Editor(override val module: Module) : Component {

    val editor: Editor = import()
    var delegate: EditorDelegate = HeadlessEditorDelegate(module)
    val isSkipUpdateSelectionModelProperty = Property<Boolean>(false)
    var isSkipUpdateSelectionModel: Boolean by isSkipUpdateSelectionModelProperty
    val isSkipUpdateSelectionUiProperty = Property<Boolean>(false)
    var isSkipUpdateSelectionUi: Boolean by isSkipUpdateSelectionUiProperty
    var runBlockingInEditorMode: EditorMode = EditorMode.Edit
    val features = mutableListOf<EditorFeature>()

    // import
    val mainStage: Stage = import(MainStageProxy)
    val project: Project = import()
    val rmlTableView: RmlTableView = import()
    val rmlTreeView: RmlTreeView = import()
    val selectionService: SelectionService = import()
    val tab: EditorTab = import()
    val window: Window by lazy { import() } // quickfix todo decouple graphics

    fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String {
        var result: String = value
        for (feature in features) {
            result = feature.filterAttribute(rmlTag, key, result)
        }
        return result
    }

    fun append(vararg rmlTags: ResourceTag) = runBlockingInEditorMode {
        with(tab.rmlResource) {
            for (rmlTag in rmlTags) {
                for (attribute in rmlTag.attributes) {
                    rmlTag.attributes[attribute.key] = filterAttribute(rmlTag, attribute.key, attribute.value)
                }
                tab.rmlResource.rmlTag.appendChild(rmlTag)
            }
            // >> just for now todo delete this
            tab.rmlResource.rmlTag.build()
            window.notifyResize()
            window.invalidate()
            // <<
        }
        runOnJfxThread {
            tab.isDirty = true
        }
        rmlTreeView.updateRmlTag()
    }

    fun remove(vararg rmlTags: ResourceTag) = runBlockingInEditorMode {
        with(tab.rmlResource) {
            for (rmlTag in rmlTags) {
                if (rmlTag != tab.rmlResource.rmlTag) {
                    selectionService.selections.remove(with(editor.delegate) { selectionOf(rmlTag) })
                    rmlTag.parent?.removeChild(rmlTag)
                }
            }
        }
        runOnJfxThread {
            tab.isDirty = true
        }
        rmlTreeView.updateRmlTag()
    }

    fun replace(rmlTag: ResourceTag, index: Int) = runBlockingInEditorMode {
        if (rmlTag == tab.rmlResource.rmlTag) return@runBlockingInEditorMode
        val parent = rmlTag.parent ?: return@runBlockingInEditorMode

        with(tab.rmlResource) {
            parent.replaceChild(index, rmlTag)
        }
        runOnJfxThread {
            tab.isDirty = true
        }
        rmlTreeView.updateRmlTag()
        rmlTreeView.select(rmlTag)
    }

    // IMPORTANT valid to invoke only inside updateEditorUI scope
    suspend fun updateModel(rmlTag: ResourceTag, vararg attributes: ResourceAttribute, isFilterEnable: Boolean = true) {
        for ((key, rawValue) in attributes) {
            val canonicalName = tab.rmlResource.rmlFile.rmlSchema.canonicalClassNameByKey["${rmlTag.name}.${key}"]
            if (rawValue.isEmpty() && canonicalName != "String") {
                with(tab.rmlResource) {
                    rmlTag.removeAttribute(key, rawValue)
                    rmlTag.build()
                }
            } else {
                with(tab.rmlResource) {
                    val value: String = if (isFilterEnable) filterAttribute(rmlTag, key, rawValue) else rawValue
                    rmlTag.buildAttribute(key, value)
                }
            }
        }

        tab.isDirty = true
        // >> quickfix todo improve
        rmlTreeView.refresh()
        rmlTableView.updateRmlTag()
        // <<
    }

    fun updateEditorUi(action: suspend () -> Unit) {
        runBlockingInEditorMode(action)
    }

    fun skipUpdateSelectionModel(action: () -> Unit) {
        isSkipUpdateSelectionModel = true
        action()
        isSkipUpdateSelectionModel = false
    }

    fun skipUpdateSelectionUI(action: () -> Unit) {
        isSkipUpdateSelectionUi = true
        action()
        isSkipUpdateSelectionUi = false
    }

}

fun Editor.findAttributeKeys(rmlTag: ResourceTag?): List<String> {
    return tab.rmlResource.rmlFile.rmlSchema.attributeNamesByRmlTagName[rmlTag?.name]
}
