package featurea.studio.editor

import featurea.content.ResourceTag
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

abstract class EditorFeature(override val module: Module) : Component {

    val editor: Editor = import()

    var isEnable: Boolean = true

    init {
        editor.features.add(this)
    }

    open fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String = value

    open fun filterAttributeKeys(rmlTag: ResourceTag, attributeKeys: MutableList<String>) {}

}
