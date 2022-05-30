package featurea.studio.editor.components

import featurea.content.ResourceTag

interface EditorListener {
    fun save(rmlTag: ResourceTag) {}
}