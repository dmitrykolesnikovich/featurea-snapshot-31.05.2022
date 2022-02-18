package featurea.studio.editor.features

import featurea.content.ResourceTag
import featurea.content.isValidImageResource
import featurea.loader.Loader
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.window.Window
import kotlinx.coroutines.runBlocking

class AutoloadTextureEditorFeature(module: Module) : EditorFeature(module) {

    private val loader: Loader = import()
    private val window: Window = import()

    override fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String {
        runBlocking {
            if (value.isValidImageResource()) {
                loader.loadRmlAttribute(rmlTag, key, value) {
                    window.invalidate()
                }
            }
        }
        return value
    }

}
