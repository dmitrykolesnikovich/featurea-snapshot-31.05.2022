package featurea.studio.editor.components

import featurea.config.Config
import featurea.desktop.jfx.toImageView
import featurea.desktop.runOnJfxThread
import featurea.jvm.normalizedPath
import featurea.rml.RmlResource
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.utils.Property
import featurea.runtime.import
import featurea.studio.project.Project
import javafx.scene.control.Tab

class EditorTab(override val module: Module) : Component {

    private val project: Project = import()

    lateinit var tab: Tab
    val isDirtyProperty = Property<Boolean>(value = false)
    var isDirty: Boolean by isDirtyProperty
    val rmlResourceProperty = Property<RmlResource>()
    var rmlResource: RmlResource by rmlResourceProperty
    val id: String get() = rmlResource.rmlTag.idOrNull ?: error("rmlTag: ${rmlResource.rmlTag}")
    val listeners = mutableListOf<EditorListener>()
    private val iconsConfig = Config("icons")

    init {
        isDirtyProperty.watch { updateTabTitle() }
        rmlResourceProperty.watch { updateTabTitle() }
    }

    fun save() {
        for (listener in listeners) {
            listener.save(rmlResource.rmlTag)
        }
        project.save(rmlResource.rmlTag)
        isDirty = false
    }

    fun resolveIcon() = runOnJfxThread {
        tab.graphic = iconsConfig[rmlResource.rmlTag.name]?.toImageView(13)
    }

    /*internals*/

    private fun updateTabTitle() = runOnJfxThread {
        if (::tab.isInitialized) if (isDirty) tab.text = "* $id" else tab.text = id
        if (project.isDirty) project.mainStage.title = "* ${project.rmlFile.normalizedPath}"
        else project.mainStage.title = project.rmlFile.normalizedPath
    }

}



