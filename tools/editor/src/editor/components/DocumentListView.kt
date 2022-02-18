package featurea.studio.editor.components

import featurea.desktop.runOnJfxThread
import featurea.rml.RmlResource
import featurea.content.ResourceTag
import featurea.content.isSuper
import featurea.runtime.Constructor
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.create
import javafx.scene.control.Label
import javafx.scene.control.ListView

class DocumentListView(override val module: Module) : ListView<String>(), Component {

    init {
        placeholder = Label("Nothing to Show")
    }

    fun update(rmlResource: RmlResource, name: String) = runOnJfxThread {
        val rmlTag = rmlResource.rmlTag
        val rmlSchema = rmlResource.rmlFile.rmlSchema
        items.clear()
        items.addAll(rmlTag.properties.values.filter { rmlSchema.isSuper(it.name, name) }.mapNotNull { it.idOrNull })
    }

    fun update(rmlTag: ResourceTag) = runOnJfxThread {
        items.clear()
        items.addAll(rmlTag.properties.values.mapNotNull { it.idOrNull })
    }

}

@Constructor
fun Component.DocumentListView(): DocumentListView = create()
